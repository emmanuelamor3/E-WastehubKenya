package com.example.e_wastehubkenya.data

import com.example.e_wastehubkenya.data.model.AccessToken
import com.example.e_wastehubkenya.data.model.STKQueryRequest
import com.example.e_wastehubkenya.data.model.STKQueryResponse
import com.example.e_wastehubkenya.data.model.StkPushRequest
import com.example.e_wastehubkenya.data.model.StkPushResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MpesaApiService {

    @GET("oauth/v1/generate?grant_type=client_credentials")
    suspend fun getAccessToken(
        @Header("Authorization") authHeader: String
    ): AccessToken

    @POST("mpesa/stkpush/v1/processrequest")
    suspend fun initiateStkPush(
        @Header("Authorization") authHeader: String,
        @Body stkPushRequest: StkPushRequest
    ): StkPushResponse

    @POST("mpesa/stkpushquery/v1/query") // Corrected endpoint
    suspend fun queryStkPushStatus(
        @Header("Authorization") authHeader: String,
        @Body stkQueryRequest: STKQueryRequest
    ): STKQueryResponse
}
