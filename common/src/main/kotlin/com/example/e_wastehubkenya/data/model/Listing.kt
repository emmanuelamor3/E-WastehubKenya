package com.example.e_wastehubkenya.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// This represents the actual E-Waste Listing object
@Serializable
data class Listing(
    @SerialName("listing_id")
    val listingId: Int,
    @SerialName("user_id")
    val userId: Int,
    @SerialName("category")
    val category: String,
    @SerialName("product_name")
    val productName: String,
    @SerialName("brand")
    val brand: String? = null,
    @SerialName("model")
    val model: String? = null,
    @SerialName("condition")
    val condition: String,
    @SerialName("serial_number")
    val serialNumber: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("price")
    val price: String,
    @SerialName("status")
    val status: String,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String
)
