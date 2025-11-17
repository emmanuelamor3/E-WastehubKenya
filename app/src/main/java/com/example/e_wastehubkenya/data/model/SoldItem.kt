package com.example.e_wastehubkenya.data.model

import java.util.Date

data class SoldItem(
    val listingId: String,
    val productName: String,
    val price: Double,
    val imageUrl: String,
    val orderId: String,
    val purchaseDate: Date?
)
