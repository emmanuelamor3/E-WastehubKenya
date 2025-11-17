package com.example.e_wastehubkenya.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.User
import com.example.e_wastehubkenya.data.repository.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _user = MutableLiveData<Resource<User>>()
    val user: LiveData<Resource<User>> = _user

    private val _seller = MutableLiveData<Resource<User>>()
    val seller: LiveData<Resource<User>> = _seller

    private val _updateUserResult = MutableLiveData<Resource<Unit>>()
    val updateUserResult: LiveData<Resource<Unit>> = _updateUserResult

    private val _uploadProfilePictureResult = MutableLiveData<Resource<String>>()
    val uploadProfilePictureResult: LiveData<Resource<String>> = _uploadProfilePictureResult

    fun fetchUser() {
        viewModelScope.launch {
            userRepository.getUser().collectLatest {
                _user.postValue(it)
            }
        }
    }

    fun fetchUserById(userId: String) {
        viewModelScope.launch {
            userRepository.getUserById(userId).collectLatest {
                _seller.postValue(it)
            }
        }
    }

    fun updateUser(name: String, phoneNumber: String) {
        viewModelScope.launch {
            userRepository.updateUser(name, phoneNumber).collectLatest {
                _updateUserResult.postValue(it)
            }
        }
    }

    fun uploadProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            userRepository.uploadProfilePicture(imageUri).collectLatest {
                _uploadProfilePictureResult.postValue(it)
            }
        }
    }
}
