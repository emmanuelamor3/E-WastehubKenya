package com.example.e_wastehubkenya.data.model

data class Listing(
    val listing_id: Int,
    val user_id: Int,
    val category: String,
    val product_name: String,
    val brand: String?,
    val model: String?,
    val condition: String,
    val serial_number: String?,
    val description: String?,
    val price: Double,
    val status: String,
    val image_url: String?,
    val created_at: String
)