package com.example.e_wastehubkenya.data.repository

import com.example.e_wastehubkenya.data.model.Listing
import com.example.e_wastehubkenya.data.model.MessageResponse
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

    /**
     * Checks the serial number against the stolen database.
     */
    fun checkSerial(serialNumber: String): Flow<Resource<MessageResponse>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.checkSerial(serialNumber)

            if (response.isSuccessful) {
                // API returns 200 OK if NOT found (safe to register)
                emit(Resource.Success(MessageResponse("Serial number is clear.")))
            } else {
                // API returns 404 (Not Found) or other error if FOUND (stolen)
                val errorMsg = response.errorBody()?.string() ?: "Product flagged as stolen."
                emit(Resource.Error(errorMsg))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Could not connect to server. Check internet."))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }

    /**
     * Uploads the new listing with all its data and the image.
     */
    fun createListing(
        productName: RequestBody,
        category: RequestBody,
        brand: RequestBody,
        model: RequestBody,
        condition: RequestBody,
        serialNumber: RequestBody,
        description: RequestBody,
        price: RequestBody,
        location: RequestBody,
        image: MultipartBody.Part
    ): Flow<Resource<Listing>> = flow {
        try {
            emit(Resource.Loading())
            val response = api.createListing(
                productName, category, brand, model, condition,
                serialNumber, description, price, location, image
            )

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