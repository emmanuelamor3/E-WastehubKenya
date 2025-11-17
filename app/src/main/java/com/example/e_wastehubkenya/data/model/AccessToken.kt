package com.example.e_wastehubkenya.data.model

import com.google.gson.annotations.SerializedName

data class AccessToken(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_in")
    val expiresIn: String
)
