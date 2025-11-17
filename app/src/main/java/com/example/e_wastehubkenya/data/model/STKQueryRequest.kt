package com.example.e_wastehubkenya.data.model

import com.google.gson.annotations.SerializedName

data class STKQueryRequest(
    @SerializedName("BusinessShortCode")
    val businessShortCode: String,
    @SerializedName("Password")
    val password: String,
    @SerializedName("Timestamp")
    val timestamp: String,
    @SerializedName("CheckoutRequestID")
    val checkoutRequestID: String
)
