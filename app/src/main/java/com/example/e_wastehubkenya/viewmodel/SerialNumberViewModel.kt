package com.example.e_wastehubkenya.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.utils.Luhn
import kotlinx.coroutines.launch

class SerialNumberViewModel : ViewModel() {

    private val _verificationResult = MutableLiveData<Resource<Boolean>>()
    val verificationResult: LiveData<Resource<Boolean>> = _verificationResult

    fun verifySerialNumber(serialNumber: String) {
        viewModelScope.launch {
            _verificationResult.postValue(Resource.Loading())
            val isValid = Luhn.isValidLuhn(serialNumber)
            if (isValid) {
                _verificationResult.postValue(Resource.Success(true))
            } else {
                _verificationResult.postValue(Resource.Error("Invalid serial number"))
            }
        }
    }
}