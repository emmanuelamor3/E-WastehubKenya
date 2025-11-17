package com.example.e_wastehubkenya.data.model

import com.google.gson.annotations.SerializedName

data class StkPushRequest(
    @SerializedName("BusinessShortCode")
    val businessShortCode: String,
    @SerializedName("Password")
    val password: String,
    @SerializedName("Timestamp")
    val timestamp: String,
    @SerializedName("TransactionType")
    val transactionType: String = "CustomerPayBillOnline",
    @SerializedName("Amount")
    val amount: String,
    @SerializedName("PartyA")
    val partyA: String,
    @SerializedName("PartyB")
    val partyB: String,
    @SerializedName("PhoneNumber")
    val phoneNumber: String,
    @SerializedName("CallBackURL")
    val callBackURL: String,
    @SerializedName("AccountReference")
    val accountReference: String,
    @SerializedName("TransactionDesc")
    val transactionDesc: String
)
