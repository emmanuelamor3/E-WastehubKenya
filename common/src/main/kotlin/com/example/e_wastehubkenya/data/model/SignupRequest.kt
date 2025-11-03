package com.example.e_wastehubkenya.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String
)