package com.example.e_wastehubkenya.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.CartItem
import com.example.e_wastehubkenya.data.model.Order
import com.example.e_wastehubkenya.data.repository.OrderRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {

    private val orderRepository = OrderRepository()

    private val _createOrderResult = MutableLiveData<Resource<String>>()
    val createOrderResult: LiveData<Resource<String>> = _createOrderResult

    private val _mySales = MutableLiveData<Resource<List<Order>>>()
    val mySales: LiveData<Resource<List<Order>>> = _mySales

    fun createOrder(items: List<CartItem>, total: Double) {
        viewModelScope.launch {
            orderRepository.createOrder(items, total).collectLatest { result ->
                _createOrderResult.postValue(result)
            }
        }
    }

    fun fetchMySales() {
        viewModelScope.launch {
            orderRepository.getMySales().collectLatest { result ->
                _mySales.postValue(result)
            }
        }
    }
}