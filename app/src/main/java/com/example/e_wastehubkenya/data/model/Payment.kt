package com.example.e_wastehubkenya.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Payment(
    val paymentId: String = "",
    val orderId: String = "",
    val amount: Double = 0.0,
    val mpesaReceiptNumber: String = "",
    val status: String = "Pending", // Pending, Success, Failed
    @ServerTimestamp
    val timestamp: Date? = null
)
