package com.example.e_wastehubkenya.data.network

import com.example.e_wastehubkenya.data.model.LoginRequest
import com.example.e_wastehubkenya.data.model.LoginResponse
import com.example.e_wastehubkenya.data.model.SignupRequest
import com.example.e_wastehubkenya.data.model.MessageResponse
import com.example.e_wastehubkenya.data.model.Listing //
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.GET
import okhttp3.MultipartBody
import okhttp3.RequestBody


interface ApiService {
    @POST("/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>
    @POST("/signup")
    suspend fun signup(
        @Body signupRequest: SignupRequest
    ): Response<MessageResponse>
    @POST("/forgot-password")
    suspend fun forgotPassword(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>
@GET("check-serial/{serial}")
suspend fun checkSerial(
    @Path("serial") serial: String
): Response<MessageResponse>
@Multipart
@POST("/listings")
suspend fun createListing(
    @Part("product_name") productName: RequestBody,
    @Part("category") category: RequestBody,
    @Part("brand") brand: RequestBody,
    @Part("model") model: RequestBody,
    @Part("condition") condition: RequestBody,
    @Part("serial_number") serialNumber: RequestBody,
    @Part("description") description: RequestBody,
    @Part("price") price: RequestBody,
    @Part("location") location: RequestBody,
    @Part image: MultipartBody.Part // The image file
): Response<Listing>


}
