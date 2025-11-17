package com.example.e_wastehubkenya.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.STKQueryResponse
import com.example.e_wastehubkenya.data.model.StkPushResponse
import com.example.e_wastehubkenya.data.repository.MpesaRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MpesaViewModel : ViewModel() {

    private val mpesaRepository = MpesaRepository()

    private val _stkPushResult = MutableLiveData<Resource<StkPushResponse>>()
    val stkPushResult: LiveData<Resource<StkPushResponse>> = _stkPushResult

    private val _stkQueryResult = MutableLiveData<Resource<STKQueryResponse>>()
    val stkQueryResult: LiveData<Resource<STKQueryResponse>> = _stkQueryResult

    fun initiateStkPush(amount: String, phoneNumber: String, accountReference: String) {
        viewModelScope.launch {
            mpesaRepository.initiateStkPush(amount, phoneNumber, accountReference).collectLatest { result ->
                _stkPushResult.postValue(result)
            }
        }
    }

    fun queryStkPushStatus(checkoutRequestID: String) {
        viewModelScope.launch {
            mpesaRepository.queryStkPushStatus(checkoutRequestID).collectLatest { result ->
                _stkQueryResult.postValue(result)
            }
        }
    }
}
