package com.example.e_wastehubkenya.data.repository

import com.example.e_wastehubkenya.data.model.LoginRequest
import com.example.e_wastehubkenya.data.model.LoginResponse
import com.example.e_wastehubkenya.data.model.SignupRequest
import com.example.e_wastehubkenya.data.model.MessageResponse
import com.example.e_wastehubkenya.data.network.ApiService
import com.example.e_wastehubkenya.data.network.RetrofitInstance
import com.example.e_wastehubkenya.data.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class AuthRepository(
    private val api: ApiService = RetrofitInstance.api
) {
    fun login(loginRequest: LoginRequest): Flow<Resource<LoginResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            }
            else {
                val errorMsg = response.errorBody()?.string() ?: "An unknown error occurred"
                emit(Resource.Error(errorMsg))
            }
        } catch(e: IOException) {
            emit(Resource.Error("Network error"))
        } catch(e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }
    fun signup(signupRequest: SignupRequest): Flow<Resource<MessageResponse>> = flow {
        try {
            // 1. Emit Loading state
            emit(Resource.Loading())

            // 2. Make the network call
            val response = api.signup(signupRequest)

            // 3. Check the response
            if (response.isSuccessful && response.body() != null) {
                // 4a. Emit Success state
                emit(Resource.Success(response.body()!!))
            } else {
                // 4b. Emit Error state
                val errorMsg = response.errorBody()?.string() ?: "An unknown error occurred"
                emit(Resource.Error(errorMsg))
            }
        } catch (e: IOException) {
            // 5. Handle network errors
            emit(Resource.Error("Could not connect to the server. Please check your internet connection."))
        } catch (e: Exception) {
            // 6. Handle other unexpected errors
            emit(Resource.Error("An unexpected error occurred: ${e.message}"))
        }
    }
}