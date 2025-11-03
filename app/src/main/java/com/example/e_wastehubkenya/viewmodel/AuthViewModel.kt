package com.example.e_wastehubkenya.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.LoginResponse
import com.example.e_wastehubkenya.data.model.MessageResponse
import com.example.e_wastehubkenya.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    // For Signup
    private val _signupResult = MutableLiveData<Resource<MessageResponse>>()
    val signupResult: LiveData<Resource<MessageResponse>> = _signupResult

    // For Login
    private val _loginResult = MutableLiveData<Resource<LoginResponse>>()
    val loginResult: LiveData<Resource<LoginResponse>> = _loginResult

    fun signupUser(name: String, email: String, phone: String, password: String, role: String) {
        viewModelScope.launch {
            _signupResult.postValue(Resource.Loading())
            val result = try {
                repository.signup(name, email, phone, password, role)
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
            _signupResult.postValue(result)
        }
    }

    fun loginUser(email: String, password: String, role: String) { // Added role
        viewModelScope.launch {
            _loginResult.postValue(Resource.Loading())
            val result = try {
                repository.login(email, password, role) // Pass role
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An error occurred")
            }
            _loginResult.postValue(result)
        }
    }
}
