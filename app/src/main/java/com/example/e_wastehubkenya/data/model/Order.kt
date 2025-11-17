package com.example.e_wastehubkenya.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val sellerIds: List<String> = emptyList(),
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val status: String = "Pending", // Pending, Paid, Failed
    @ServerTimestamp
    val timestamp: Date? = null
)
