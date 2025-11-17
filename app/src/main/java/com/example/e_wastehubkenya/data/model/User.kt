package com.example.e_wastehubkenya.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val role: String = "Buyer" // Added user role, defaults to Buyer
)