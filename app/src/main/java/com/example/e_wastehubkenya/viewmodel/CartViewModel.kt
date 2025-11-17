package com.example.e_wastehubkenya.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.CartItem
import com.example.e_wastehubkenya.data.repository.CartRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    private val cartRepository = CartRepository()

    private val _addToCartResult = MutableLiveData<Resource<Unit>>()
    val addToCartResult: LiveData<Resource<Unit>> = _addToCartResult

    private val _cartItems = MutableLiveData<Resource<List<CartItem>>>()
    val cartItems: LiveData<Resource<List<CartItem>>> = _cartItems

    private val _removeFromCartResult = MutableLiveData<Resource<Unit>>()
    val removeFromCartResult: LiveData<Resource<Unit>> = _removeFromCartResult

    fun addToCart(cartItem: CartItem) {
        viewModelScope.launch {
            cartRepository.addToCart(cartItem).collectLatest { result ->
                _addToCartResult.postValue(result)
            }
        }
    }

    fun fetchCartItems() {
        viewModelScope.launch {
            cartRepository.getCartItems().collectLatest { result ->
                _cartItems.postValue(result)
            }
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            cartRepository.removeFromCart(cartItem).collectLatest { result ->
                _removeFromCartResult.postValue(result)
            }
        }
    }
}
