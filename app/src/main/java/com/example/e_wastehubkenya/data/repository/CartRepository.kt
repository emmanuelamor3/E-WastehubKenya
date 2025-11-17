package com.example.e_wastehubkenya.data.repository

import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class CartRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun addToCart(cartItem: CartItem): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val finalCartItem = cartItem.copy(userId = userId)

        db.collection("cart").document(finalCartItem.listingId).set(finalCartItem).await()

        emit(Resource.Success(Unit))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "An unknown error occurred"))
    }

    fun getCartItems(): Flow<Resource<List<CartItem>>> = flow {
        emit(Resource.Loading())

        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")

        val snapshot = db.collection("cart")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val cartItems = snapshot.toObjects(CartItem::class.java)
        emit(Resource.Success(cartItems))
    }.catch { e ->
        emit(Resource.Error(e.message ?: "An unknown error occurred"))
    }

    fun removeFromCart(cartItem: CartItem): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            db.collection("cart").document(cartItem.listingId).delete().await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }
}
