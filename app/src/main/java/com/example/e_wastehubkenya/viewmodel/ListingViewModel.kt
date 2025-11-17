package com.example.e_wastehubkenya.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.Listing
import com.example.e_wastehubkenya.data.repository.ListingRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ListingViewModel : ViewModel() {

    private val listingRepository = ListingRepository()

    private val _createListingResult = MutableLiveData<Resource<String>>()
    val createListingResult: LiveData<Resource<String>> = _createListingResult

    private val _deleteListingResult = MutableLiveData<Resource<Unit>>()
    val deleteListingResult: LiveData<Resource<Unit>> = _deleteListingResult

    private val _approvePurchaseResult = MutableLiveData<Resource<Unit>>()
    val approvePurchaseResult: LiveData<Resource<Unit>> = _approvePurchaseResult

    private val _myListings = MutableLiveData<Resource<List<Listing>>>()
    val myListings: LiveData<Resource<List<Listing>>> = _myListings

    private val _allListings = MutableLiveData<Resource<List<Listing>>>()
    val allListings: LiveData<Resource<List<Listing>>> = _allListings

    private val _listing = MutableLiveData<Resource<Listing>>()
    val listing: LiveData<Resource<Listing>> = _listing

    private val _searchedListings = MutableLiveData<Resource<List<Listing>>>()
    val searchedListings: LiveData<Resource<List<Listing>>> = _searchedListings

    fun createListing(listing: Listing, imageUris: List<Uri>) {
        viewModelScope.launch {
            listingRepository.createListing(listing, imageUris).collectLatest { result ->
                _createListingResult.postValue(result)
            }
        }
    }

    fun deleteListing(listing: Listing) {
        viewModelScope.launch {
            listingRepository.deleteListing(listing).collectLatest { result ->
                _deleteListingResult.postValue(result)
            }
        }
    }

    fun approvePurchase(listingId: String, buyerId: String) {
        viewModelScope.launch {
            listingRepository.approvePurchase(listingId, buyerId).collectLatest { result ->
                _approvePurchaseResult.postValue(result)
            }
        }
    }

    fun incrementViewCount(listingId: String, viewerId: String) {
        viewModelScope.launch {
            listingRepository.incrementViewCount(listingId, viewerId).collectLatest { /* No-op */ }
        }
    }

    fun getListing(listingId: String) {
        viewModelScope.launch {
            listingRepository.getListing(listingId).collectLatest { result ->
                _listing.postValue(result)
            }
        }
    }

    fun searchListings(query: String) {
        viewModelScope.launch {
            listingRepository.searchListings(query).collectLatest { result ->
                _searchedListings.postValue(result)
            }
        }
    }

    fun fetchMyListings() {
        viewModelScope.launch {
            listingRepository.getMyListings().collectLatest { result ->
                _myListings.postValue(result)
            }
        }
    }

    fun fetchAllListings() {
        viewModelScope.launch {
            listingRepository.getAllListings().collectLatest { result ->
                _allListings.postValue(result)
            }
        }
    }
}