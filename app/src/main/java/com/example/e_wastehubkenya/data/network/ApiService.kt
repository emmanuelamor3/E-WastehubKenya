package com.example.e_wastehubkenya.data.network

import com.example.e_wastehubkenya.data.model.Listing
import com.example.e_wastehubkenya.data.model.LoginRequest
import com.example.e_wastehubkenya.data.model.LoginResponse
import com.example.e_wastehubkenya.data.model.MessageResponse
import com.example.e_wastehubkenya.data.model.SignupRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface ApiService {
    @POST("/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("/signup")
    suspend fun signup(
        @Body signupRequest: SignupRequest
    ): Response<MessageResponse>

    @Multipart
    @POST("/listings")
    suspend fun createListing(
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part image: MultipartBody.Part
    ): Response<Listing>
}
