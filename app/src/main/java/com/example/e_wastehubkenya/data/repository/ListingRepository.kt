package com.example.e_wastehubkenya.data.repository

import android.net.Uri
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.Listing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ListingRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun createListing(
        listing: Listing,
        imageUris: List<Uri>
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val imageUrls = uploadImages(imageUris)

        val newListingRef = db.collection("listings").document()
        val listingId = newListingRef.id

        val finalListing = listing.copy(
            id = listingId,
            userId = userId,
            imageUrls = imageUrls,
            timestamp = System.currentTimeMillis()
        )

        newListingRef.set(finalListing).await()

        emit(Resource.Success("Listing created successfully!"))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "An unknown error occurred"))
    }

    fun deleteListing(listing: Listing): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            // Delete images from storage
            for (imageUrl in listing.imageUrls) {
                val imageRef = storage.getReferenceFromUrl(imageUrl)
                imageRef.delete().await()
            }

            // Delete listing from Firestore
            db.collection("listings").document(listing.id).delete().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }

    fun approvePurchase(listingId: String, buyerId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            db.collection("listings").document(listingId)
                .update("approvedBuyerId", buyerId)
                .await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }

    fun incrementViewCount(listingId: String, viewerId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val listingRef = db.collection("listings").document(listingId)
            val snapshot = listingRef.get().await()
            val listing = snapshot.toObject(Listing::class.java)

            if (listing != null && !listing.viewedBy.contains(viewerId) && listing.userId != viewerId) {
                listingRef.update(
                    "viewCount", FieldValue.increment(1),
                    "viewedBy", FieldValue.arrayUnion(viewerId)
                ).await()
            }
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }

    fun getListing(listingId: String): Flow<Resource<Listing>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = db.collection("listings").document(listingId).get().await()
            val listing = snapshot.toObject(Listing::class.java)?.copy(id = snapshot.id)
            if (listing != null) {
                emit(Resource.Success(listing))
            } else {
                emit(Resource.Error("Listing not found"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }

    private suspend fun uploadImages(imageUris: List<Uri>): List<String> {
        val imageUrls = mutableListOf<String>()
        for (uri in imageUris) {
            val storageRef = storage.reference
            val imageRef = storageRef.child("images/${UUID.randomUUID()}")
            imageRef.putFile(uri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()
            imageUrls.add(downloadUrl)
        }
        return imageUrls
    }

    fun searchListings(query: String): Flow<Resource<List<Listing>>> = flow {
        emit(Resource.Loading())
        try {
            val snapshot = db.collection("listings")
                .orderBy("productName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()

            val listings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Listing::class.java)?.copy(id = doc.id)
            }
            emit(Resource.Success(listings))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }

    fun getMyListings(): Flow<Resource<List<Listing>>> = flow {
        emit(Resource.Loading())

        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val snapshot = db.collection("listings")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        val listings = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Listing::class.java)?.copy(id = doc.id)
        }
        emit(Resource.Success(listings))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "An unknown error occurred"))
    }

    fun getAllListings(): Flow<Resource<List<Listing>>> = flow {
        emit(Resource.Loading())

        val snapshot = db.collection("listings")
            .whereEqualTo("status", "Available") // Only fetch available listings
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()

        val listings = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Listing::class.java)?.copy(id = doc.id)
        }
        emit(Resource.Success(listings))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "An unknown error occurred"))
    }
}
