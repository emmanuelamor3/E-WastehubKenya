package com.example.e_wastehubkenya.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Listing(
    var id: String = "",
    val userId: String = "",
    val productName: String = "",
    val category: String = "",
    val brand: String = "",
    val model: String = "",
    val condition: String = "",
    val serialNumber: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val location: String = "",
    val imageUrls: List<String> = emptyList(),
    val viewCount: Int = 0,
    val viewedBy: List<String> = emptyList(),
    val status: String = "Available", // New status field
    val timestamp: Long = 0L,
    val isDonation: Boolean = false,
    val approvedBuyerId: String? = null
) : Parcelable
