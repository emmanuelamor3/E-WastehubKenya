package com.example.e_wastehubkenya.data.network

import com.example.e_wastehubkenya.data.model.LoginRequest
import com.example.e_wastehubkenya.data.model.LoginResponse
import com.example.e_wastehubkenya.data.model.SignupRequest
import com.example.e_wastehubkenya.data.model.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>
    @POST("/api/signup")
    suspend fun signup(
        @Body signupRequest: SignupRequest
    ): Response<MessageResponse>
    @POST("/api/forgot-password")
    suspend fun forgotPassword(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

}



