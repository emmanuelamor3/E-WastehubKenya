package com.example.e_wastehubkenya.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wastehubkenya.data.model.LoginRequest
import com.example.e_wastehubkenya.data.model.LoginResponse
import com.example.e_wastehubkenya.data.model.SignupRequest
import com.example.e_wastehubkenya.data.model.MessageResponse
import com.example.e_wastehubkenya.data.repository.AuthRepository
import com.example.e_wastehubkenya.data.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val _loginResult = MutableLiveData<Resource<LoginResponse>>()
    val loginResult: MutableLiveData<Resource<LoginResponse>> get() = _loginResult

    private val _signupResult = MutableLiveData<Resource<MessageResponse>>()
    val signupResult: LiveData<Resource<MessageResponse>> get() = _signupResult

    fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)
        viewModelScope.launch {
            authRepository.login(loginRequest).collectLatest { result ->
                _loginResult.postValue(result)
            }
        }

    }
    fun signupUser(name: String, email: String, phone: String, password: String, role: String) {
        // You can add validation here or in the Activity
        val signupRequest = SignupRequest(name, email, phone, password, role)

        viewModelScope.launch {
            authRepository.signup(signupRequest).collectLatest { result ->
                // Post the result to the new LiveData
                _signupResult.postValue(result)
            }
        }
    }
}