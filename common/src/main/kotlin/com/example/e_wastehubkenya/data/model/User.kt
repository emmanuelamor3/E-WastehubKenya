package com.example.e_wastehubkenya.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userid: Int,
    val username: String,
    val email: String,
    val phone: String,
    val role: String
)
