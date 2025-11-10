package com.example.e_wastehubkenya.data.repository

import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.LoginRequest
import com.example.e_wastehubkenya.data.model.LoginResponse
import com.example.e_wastehubkenya.data.model.MessageResponse
import com.example.e_wastehubkenya.data.model.SignupRequest
import com.example.e_wastehubkenya.data.network.RetrofitInstance

class AuthRepository {

    private val api = RetrofitInstance.api

    suspend fun signup(name: String, email: String, phone: String, password: String, role: String): Resource<MessageResponse> {
        return try {
            val response = api.signup(SignupRequest(name, email, phone, password, role))
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "An unknown error occurred"
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An exception occurred")
        }
    }

    suspend fun login(email: String, password: String, role: String): Resource<LoginResponse> { // Added role
        return try {
            val response = api.login(LoginRequest(email, password, role)) // Pass role
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "An unknown error occurred"
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An exception occurred")
        }
    }
}