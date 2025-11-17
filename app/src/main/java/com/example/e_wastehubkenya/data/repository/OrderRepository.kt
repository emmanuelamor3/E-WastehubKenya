package com.example.e_wastehubkenya.data.repository

import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.CartItem
import com.example.e_wastehubkenya.data.model.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class OrderRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun createOrder(items: List<CartItem>, total: Double): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val orderId = db.collection("orders").document().id
            val sellerIds = items.map { it.sellerId }.distinct()

            val order = Order(
                orderId = orderId,
                userId = userId,
                sellerIds = sellerIds,
                items = items,
                total = total,
                status = "Paid"
            )

            db.runBatch { batch ->
                // 1. Create the new order
                batch.set(db.collection("orders").document(orderId), order)

                // 2. Delete items from the cart
                items.forEach { cartItem ->
                    val cartItemRef = db.collection("cart").document(cartItem.listingId)
                    batch.delete(cartItemRef)
                }

                // 3. Mark listings as "Sold"
                items.forEach { cartItem ->
                    val listingRef = db.collection("listings").document(cartItem.listingId)
                    batch.update(listingRef, "status", "Sold")
                }
            }.await()

            emit(Resource.Success("Order placed successfully!"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to place order"))
        }
    }

    fun getMySales(): Flow<Resource<List<Order>>> = flow {
        emit(Resource.Loading())
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val snapshot = db.collection("orders")
                .whereArrayContains("sellerIds", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.toObjects(Order::class.java)
            emit(Resource.Success(orders))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }
}