package com.example.e_wastehubkenya.data.model

import com.google.gson.annotations.SerializedName

data class STKQueryResponse(
    @SerializedName("ResponseCode")
    val responseCode: String,
    @SerializedName("ResponseDescription")
    val responseDescription: String,
    @SerializedName("MerchantRequestID")
    val merchantRequestID: String,
    @SerializedName("CheckoutRequestID")
    val checkoutRequestID: String,
    @SerializedName("ResultCode")
    val resultCode: String, // 0 for success
    @SerializedName("ResultDesc")
    val resultDesc: String
)
