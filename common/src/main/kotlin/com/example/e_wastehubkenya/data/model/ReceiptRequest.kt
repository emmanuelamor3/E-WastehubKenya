package com.example.e_wastehubkenya.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReceiptRequest(
    val email: String,
    val orderDetails: String
)
