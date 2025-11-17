package com.example.e_wastehubkenya.data.repository

import android.util.Base64
import com.example.e_wastehubkenya.BuildConfig
import com.example.e_wastehubkenya.data.MpesaApiService
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.AccessToken
import com.example.e_wastehubkenya.data.model.STKQueryRequest
import com.example.e_wastehubkenya.data.model.STKQueryResponse
import com.example.e_wastehubkenya.data.model.StkPushRequest
import com.example.e_wastehubkenya.data.model.StkPushResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MpesaRepository {

    private val mpesaApiService: MpesaApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://sandbox.safaricom.co.ke/")
            .client(okHttpClient) // Add the custom client
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MpesaApiService::class.java)
    }

    private suspend fun getAccessToken(): Resource<AccessToken> {
        return try {
            val consumerKey = BuildConfig.MPESA_CONSUMER_KEY
            val consumerSecret = BuildConfig.MPESA_CONSUMER_SECRET
            val authHeader = "Basic " + Base64.encodeToString("$consumerKey:$consumerSecret".toByteArray(), Base64.NO_WRAP)
            val accessToken = mpesaApiService.getAccessToken(authHeader)
            Resource.Success(accessToken)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    fun initiateStkPush(
        amount: String,
        phoneNumber: String,
        accountReference: String
    ): Flow<Resource<StkPushResponse>> = flow {
        emit(Resource.Loading())
        when (val tokenResource = getAccessToken()) {
            is Resource.Success -> {
                val token = tokenResource.data!!
                val timestamp = getTimestamp()
                val shortCode = "174379" // Your paybill or till number
                val password = getPassword(shortCode, "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919", timestamp) // Passkey

                val request = StkPushRequest(
                    businessShortCode = shortCode,
                    password = password,
                    timestamp = timestamp,
                    amount = amount,
                    partyA = phoneNumber,
                    partyB = shortCode,
                    phoneNumber = phoneNumber,
                    callBackURL = "https://jana-noncotyledonous-jonas.ngrok-free.dev/mpesa-callback", // Your callback URL
                    accountReference = accountReference,
                    transactionDesc = "E-Waste Hub Purchase"
                )

                try {
                    val stkPushResponse = mpesaApiService.initiateStkPush("Bearer ${token.accessToken}", request)
                    emit(Resource.Success(stkPushResponse))
                } catch (e: Exception) {
                    emit(Resource.Error(e.message ?: "An unknown error occurred"))
                }
            }
            is Resource.Error -> {
                emit(Resource.Error(tokenResource.message ?: "Failed to get access token"))
            }
            else -> { /* No-op */ }
        }
    }

    fun queryStkPushStatus(checkoutRequestID: String): Flow<Resource<STKQueryResponse>> = flow {
        emit(Resource.Loading())
        when (val tokenResource = getAccessToken()) {
            is Resource.Success -> {
                val token = tokenResource.data!!
                val timestamp = getTimestamp()
                val shortCode = "174379"
                val password = getPassword(shortCode, "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919", timestamp)

                val request = STKQueryRequest(
                    businessShortCode = shortCode,
                    password = password,
                    timestamp = timestamp,
                    checkoutRequestID = checkoutRequestID
                )

                try {
                    val response = mpesaApiService.queryStkPushStatus("Bearer ${token.accessToken}", request)
                    emit(Resource.Success(response))
                } catch (e: Exception) {
                    emit(Resource.Error(e.message ?: "Failed to query status"))
                }
            }
            is Resource.Error -> {
                emit(Resource.Error(tokenResource.message ?: "Failed to get access token"))
            }
            else -> { /* No-op */ }
        }
    }

    private fun getTimestamp(): String {
        return SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
    }

    private fun getPassword(shortCode: String, passkey: String, timestamp: String): String {
        val combined = "$shortCode$passkey$timestamp"
        return Base64.encodeToString(combined.toByteArray(), Base64.NO_WRAP)
    }
}
