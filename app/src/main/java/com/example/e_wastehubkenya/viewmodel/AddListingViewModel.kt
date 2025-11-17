package com.example.e_wastehubkenya.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.e_wastehubkenya.data.model.Listing

class AddListingViewModel : ViewModel() {

    // Holds the listing data as it's being built across the steps
    private val _listingInProgress = MutableLiveData(Listing())
    val listingInProgress: LiveData<Listing> = _listingInProgress

    // Holds the URIs of the selected images
    private val _imageUris = MutableLiveData<List<Uri>>(emptyList())
    val imageUris: LiveData<List<Uri>> = _imageUris

    fun updatePartialListing(listing: Listing) {
        _listingInProgress.value = listing
    }

    fun updateImageUris(uris: List<Uri>) {
        _imageUris.value = uris
    }

    fun clear() {
        _listingInProgress.value = Listing()
        _imageUris.value = emptyList()
    }
}