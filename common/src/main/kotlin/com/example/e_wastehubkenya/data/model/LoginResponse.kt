package com.example.e_wastehubkenya.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String?,
    val message: String,
    val role: String? // Added role
)
