package com.example.e_wastehubkenya.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val listingId: String = "",
    val sellerId: String = "",
    val productName: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    var userId: String = "" // ID of the user who added to cart
) : Parcelable
