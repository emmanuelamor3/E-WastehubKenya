package com.example.e_wastehubkenya.data.model

data class SignupRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String
)