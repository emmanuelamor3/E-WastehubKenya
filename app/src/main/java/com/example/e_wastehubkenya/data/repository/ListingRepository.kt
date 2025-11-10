package com.example.e_wastehubkenya.data.repository

import com.example.e_wastehubkenya.data.model.Listing
import com.example.e_wastehubkenya.data.network.ApiService
import com.example.e_wastehubkenya.data.network.RetrofitInstance
import com.example.e_wastehubkenya.data.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.IOException

class ListingRepository(
    private val api: ApiService = RetrofitInstance.api
) {

    fun createListing(
        parts: Map<String, RequestBody>,
        image: MultipartBody.Part
    ): Flow<Resource<Listing>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.createListing(parts, image)

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to create listing."
                emit(Resource.Error(errorMsg))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Could not connect to server. Check internet."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
}