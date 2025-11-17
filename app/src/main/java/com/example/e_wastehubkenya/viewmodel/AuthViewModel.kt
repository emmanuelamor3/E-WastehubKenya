package com.example.e_wastehubkenya.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.User
import com.example.e_wastehubkenya.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _loginResult = MutableLiveData<Resource<String>>()
    val loginResult: LiveData<Resource<String>> = _loginResult

    private val _registerResult = MutableLiveData<Resource<String>>()
    val registerResult: LiveData<Resource<String>> = _registerResult

    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> = _userRole

    private val _deleteAccountResult = MutableLiveData<Resource<Unit>>()
    val deleteAccountResult: LiveData<Resource<Unit>> = _deleteAccountResult

    private val _changePasswordResult = MutableLiveData<Resource<String>>()
    val changePasswordResult: LiveData<Resource<String>> = _changePasswordResult

    init {
        checkUserLoggedIn()
    }

    fun loginUser(email: String, pass: String) {
        viewModelScope.launch {
            authRepository.loginUser(email, pass).collectLatest {
                _loginResult.postValue(it)
                if (it is Resource.Success) {
                    fetchUserRole()
                }
            }
        }
    }

    fun registerUser(user: User, pass: String) {
        viewModelScope.launch {
            authRepository.registerUser(user, pass).collectLatest {
                _registerResult.postValue(it)
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            authRepository.changePassword(currentPassword, newPassword).collectLatest {
                _changePasswordResult.postValue(it)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            authRepository.deleteAccount().collectLatest {
                _deleteAccountResult.postValue(it)
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    private fun checkUserLoggedIn() {
        if (auth.currentUser != null) {
            fetchUserRole()
        }
    }

    private fun fetchUserRole() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val snapshot = db.collection("users").document(userId).get().await()
                val user = snapshot.toObject(User::class.java)
                _userRole.postValue(user?.role)
            } catch (e: Exception) {
                _userRole.postValue(null)
            }
        }
    }
}