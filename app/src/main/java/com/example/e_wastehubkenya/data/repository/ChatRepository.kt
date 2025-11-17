package com.example.e_wastehubkenya.data.repository

import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.ChatChannel
import com.example.e_wastehubkenya.data.model.ChatMessage
import com.example.e_wastehubkenya.data.model.Listing
import com.example.e_wastehubkenya.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getOrCreateChatChannel(sellerId: String, listingId: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val currentUserId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val channelId = if (currentUserId > sellerId) "${currentUserId}_${sellerId}_${listingId}" else "${sellerId}_${currentUserId}_${listingId}"
            val channelRef = db.collection("chatChannels").document(channelId)
            val snapshot = channelRef.get().await()

            if (snapshot.exists()) {
                emit(Resource.Success(channelId))
            } else {
                // Create the channel
                val currentUserDoc = db.collection("users").document(currentUserId).get().await()
                val sellerDoc = db.collection("users").document(sellerId).get().await()
                val listingDoc = db.collection("listings").document(listingId).get().await()

                val currentUser = currentUserDoc.toObject(User::class.java) ?: throw Exception("Current user not found")
                val seller = sellerDoc.toObject(User::class.java) ?: throw Exception("Seller not found")
                val listing = listingDoc.toObject(Listing::class.java) ?: throw Exception("Listing not found")

                val newChannel = ChatChannel(
                    channelId = channelId,
                    userIds = listOf(currentUserId, sellerId),
                    userNames = mapOf(currentUserId to currentUser.name, sellerId to seller.name),
                    userProfilePictures = mapOf(currentUserId to currentUser.profilePictureUrl, sellerId to seller.profilePictureUrl),
                    unreadCount = mapOf(currentUserId to 0, sellerId to 0),
                    listingId = listingId,
                    listingImageUrl = listing.imageUrls.firstOrNull() ?: "",
                    listingName = listing.productName
                )
                channelRef.set(newChannel).await()
                emit(Resource.Success(channelId))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }


    fun sendMessage(channelId: String, message: ChatMessage): Flow<Resource<Unit>> = callbackFlow {
        val channelRef = db.collection("chatChannels").document(channelId)
        val messagesRef = channelRef.collection("messages")

        db.runBatch {
            messagesRef.add(message)
            channelRef.update(
                "lastMessage", message.message,
                "lastMessageTimestamp", FieldValue.serverTimestamp(),
                "unreadCount.${message.receiverId}", FieldValue.increment(1)
            )
        }.addOnSuccessListener {
            trySend(Resource.Success(Unit))
        }.addOnFailureListener { e ->
            trySend(Resource.Error(e.message ?: "Failed to send message"))
        }
        awaitClose { close() }
    }

    fun getMessages(channelId: String): Flow<Resource<List<ChatMessage>>> = callbackFlow {
        val messagesRef = db.collection("chatChannels").document(channelId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val listener = messagesRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Resource.Error(e.message ?: "Failed to get messages"))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val messages = snapshot.toObjects(ChatMessage::class.java)
                trySend(Resource.Success(messages))
            }
        }

        awaitClose { listener.remove() }
    }

    fun getChatChannels(): Flow<Resource<List<ChatChannel>>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: run {
            trySend(Resource.Error("User not logged in"))
            return@callbackFlow
        }

        val channelsRef = db.collection("chatChannels")
            .whereArrayContains("userIds", currentUserId)

        val listener = channelsRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                trySend(Resource.Error(e.message ?: "Failed to get channels"))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val channels = snapshot.toObjects(ChatChannel::class.java)
                trySend(Resource.Success(channels))
            }
        }

        awaitClose { listener.remove() }
    }

    fun markMessagesAsRead(channelId: String) {
        val channelRef = db.collection("chatChannels").document(channelId)
        val currentUserId = auth.currentUser?.uid ?: return

        channelRef.update("unreadCount.$currentUserId", 0)
    }

    fun getChannelDetails(channelId: String): Flow<Resource<ChatChannel>> = callbackFlow {
        val listener = db.collection("chatChannels").document(channelId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(Resource.Error(e.message ?: "Failed to get channel details"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val channel = snapshot.toObject(ChatChannel::class.java)
                    trySend(Resource.Success(channel!!))
                } else {
                    trySend(Resource.Error("Channel not found"))
                }
            }
        awaitClose { listener.remove() }
    }
}