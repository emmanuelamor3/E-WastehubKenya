package com.example.e_wastehubkenya.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wastehubkenya.data.model.Listing
import com.example.e_wastehubkenya.data.model.MessageResponse
import com.example.e_wastehubkenya.data.repository.ListingRepository
import com.example.e_wastehubkenya.data.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ListingViewModel : ViewModel() {

    private val listingRepository = ListingRepository()

    // LiveData for the serial check result
    private val _serialCheckResult = MutableLiveData<Resource<MessageResponse>>()
    val serialCheckResult: LiveData<Resource<MessageResponse>> = _serialCheckResult

    // LiveData for the create listing result
    private val _createListingResult = MutableLiveData<Resource<Listing>>()
    val createListingResult: LiveData<Resource<Listing>> = _createListingResult

    /**
     * Called when the user clicks the "Check Serial" button.
     */
    fun checkSerialNumber(serial: String) {
        viewModelScope.launch {
            listingRepository.checkSerial(serial).collectLatest { result ->
                _serialCheckResult.postValue(result)
            }
        }
    }

    /**
     * Called when the user clicks the "Submit Listing" button.
     * This function will need all the form data and the image URI.
     * Note: The conversion from String to RequestBody and Uri to MultipartBody.Part
     * will happen in the Fragment, as it needs the application Context.
     */
    fun submitListing(
        productName: RequestBody,
        category: RequestBody,
        brand: RequestBody,
        model: RequestBody,
        condition: RequestBody,
        serialNumber: RequestBody,
        description: RequestBody,
        price: RequestBody,
        location: RequestBody,
        image: MultipartBody.Part
    ) {
        viewModelScope.launch {
            listingRepository.createListing(
                productName, category, brand, model, condition,
                serialNumber, description, price, location, image
            ).collectLatest { result ->
                _createListingResult.postValue(result)
            }
        }
    }
}