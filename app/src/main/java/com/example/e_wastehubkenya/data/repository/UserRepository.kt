package com.example.e_wastehubkenya.data.repository

import android.net.Uri
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getUser(): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val snapshot = db.collection("users").document(userId).get().await()
        val user = snapshot.toObject(User::class.java) ?: throw Exception("User not found")
        emit(Resource.Success(user))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "An unknown error occurred"))
    }

    fun getUserById(userId: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        val snapshot = db.collection("users").document(userId).get().await()
        val user = snapshot.toObject(User::class.java) ?: throw Exception("User not found")
        emit(Resource.Success(user))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "An unknown error occurred"))
    }

    fun updateUser(name: String, phoneNumber: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val userRef = db.collection("users").document(userId)
        val updates = mapOf(
            "name" to name,
            "phoneNumber" to phoneNumber
        )
        userRef.update(updates).await()
        emit(Resource.Success(Unit))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "An unknown error occurred"))
    }

    fun uploadProfilePicture(imageUri: Uri): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val storageRef = storage.reference.child("profile_pictures/$userId")
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            db.collection("users").document(userId).update("profilePictureUrl", downloadUrl).await()

            emit(Resource.Success(downloadUrl))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to upload profile picture"))
        }
    }
}