package com.example.e_wastehubkenya.data.model

import com.google.gson.annotations.SerializedName

data class StkPushResponse(
    @SerializedName("MerchantRequestID")
    val merchantRequestID: String,
    @SerializedName("CheckoutRequestID")
    val checkoutRequestID: String,
    @SerializedName("ResponseCode")
    val responseCode: String,
    @SerializedName("ResponseDescription")
    val responseDescription: String,
    @SerializedName("CustomerMessage")
    val customerMessage: String
)
