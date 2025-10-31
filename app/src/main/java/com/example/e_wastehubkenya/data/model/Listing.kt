package com.example.e_wastehubkenya.data.model

import com.google.gson.annotations.SerializedName

// This represents the actual E-Waste Listing object
data class Listing(
    @SerializedName("listing_id")
    val listingId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("category")
    val category: String,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("brand")
    val brand: String?,
    @SerializedName("model")
    val model: String?,
    @SerializedName("condition")
    val condition: String,
    @SerializedName("serial_number")
    val serialNumber: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("price")
    val price: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("created_at")
    val createdAt: String
)
