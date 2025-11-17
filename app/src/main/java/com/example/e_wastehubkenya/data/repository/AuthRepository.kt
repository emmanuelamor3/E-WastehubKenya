package com.example.e_wastehubkenya.data.repository

import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun loginUser(email: String, pass: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        auth.signInWithEmailAndPassword(email, pass).await()
        emit(Resource.Success("Login successful!"))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "An unknown error occurred"))
    }

    fun registerUser(user: User, pass: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        val result = auth.createUserWithEmailAndPassword(user.email, pass).await()
        val firebaseUser = result.user ?: throw Exception("User could not be created")

        val finalUser = user.copy(uid = firebaseUser.uid)
        db.collection("users").document(firebaseUser.uid).set(finalUser).await()

        emit(Resource.Success("Registration successful!"))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "An unknown error occurred"))
    }

    fun changePassword(currentPassword: String, newPassword: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            // Re-authenticate the user
            user.reauthenticate(credential).await()

            // Update the password
            user.updatePassword(newPassword).await()

            emit(Resource.Success("Password updated successfully!"))
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthRecentLoginRequiredException -> "This operation is sensitive and requires recent authentication. Please log out and log back in before trying again."
                else -> e.message ?: "An unknown error occurred"
            }
            emit(Resource.Error(errorMessage))
        }
    }

    fun deleteAccount(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            val userId = user.uid

            db.collection("users").document(userId).delete().await()
            user.delete().await()

            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthRecentLoginRequiredException -> "This operation is sensitive and requires recent authentication. Please log out and log back in before trying again."
                else -> e.message ?: "An unknown error occurred"
            }
            emit(Resource.Error(errorMessage))
        }
    }

    fun logout() {
        auth.signOut()
    }
}
