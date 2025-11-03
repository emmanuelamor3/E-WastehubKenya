package com.example.e_wastehubkenya.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wastehubkenya.data.model.Listing
import com.example.e_wastehubkenya.data.repository.ListingRepository
import com.example.e_wastehubkenya.data.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ListingViewModel(private val context: Context) : ViewModel() {

    private val listingRepository = ListingRepository()

    private val _createListingResult = MutableLiveData<Resource<Listing>>()
    val createListingResult: LiveData<Resource<Listing>> = _createListingResult

    fun submitListing(
        parts: Map<String, RequestBody>,
        image: MultipartBody.Part
    ) {
        viewModelScope.launch {
            listingRepository.createListing(parts, image).collectLatest { result ->
                _createListingResult.postValue(result)
            }
        }
    }
}