package com.example.e_wastehubkenya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // ListingViewModel now has an empty constructor
            return ListingViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
