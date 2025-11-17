package com.example.e_wastehubkenya.ui.theme.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.Listing
import com.example.e_wastehubkenya.databinding.FragmentMylistingsBinding
import com.example.e_wastehubkenya.viewmodel.ListingViewModel
import com.example.e_wastehubkenya.viewmodel.UserViewModel

class myListingsFragment : Fragment() {

    private var _binding: FragmentMylistingsBinding? = null
    private val binding get() = _binding!!

    private val listingViewModel: ListingViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var myListingsAdapter: MyListingsAdapter
    private var allListings: List<Listing> = emptyList()
    private var currentSort: Sort = Sort.None

    sealed class Sort {
        object None : Sort()
        object PriceHighToLow : Sort()
        object PriceLowToHigh : Sort()
        object DateNewest : Sort()
        object DateOldest : Sort()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMylistingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        observeViewModel()
        userViewModel.fetchUser()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                listingViewModel.searchListings(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                listingViewModel.searchListings(newText.orEmpty())
                return true
            }
        })

        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }


    private fun sortAndDisplayListings(listings: List<Listing>) {
        val sortedList = when (currentSort) {
            Sort.PriceHighToLow -> listings.sortedByDescending { it.price }
            Sort.PriceLowToHigh -> listings.sortedBy { it.price }
            Sort.DateNewest -> listings.sortedByDescending { it.timestamp }
            Sort.DateOldest -> listings.sortedBy { it.timestamp }
            Sort.None -> listings
        }
        myListingsAdapter.updateListings(sortedList)
    }

    private fun showFilterDialog() {
        // ... (filter dialog logic) ...
    }

    private fun setupViewPager() {
        myListingsAdapter = MyListingsAdapter(emptyList()) { listing ->
            val bundle = Bundle().apply {
                putParcelable("listing", listing)
            }
            findNavController().navigate(R.id.action_myListingsFragment_to_listingDetailsFragment, bundle)
        }
        binding.viewPagerListings.adapter = myListingsAdapter
    }

    private fun observeViewModel() {
        userViewModel.user.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    val user = resource.data
                    binding.searchView.isVisible = user?.role == "Buyer"
                    binding.btnFilter.isVisible = user?.role == "Buyer"
                    if (user?.role == "Seller") {
                        (activity as? DashboardActivity)?.supportActionBar?.title = "My Listings"
                        listingViewModel.fetchMyListings()
                    } else {
                        (activity as? DashboardActivity)?.supportActionBar?.title = "Shop"
                        listingViewModel.fetchAllListings()
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        listingViewModel.myListings.observe(viewLifecycleOwner) { resource ->
            if (userViewModel.user.value?.data?.role == "Seller") {
                handleListingsResource(resource)
            }
        }

        listingViewModel.allListings.observe(viewLifecycleOwner) { resource ->
            if (userViewModel.user.value?.data?.role == "Buyer") {
                handleListingsResource(resource)
            }
        }

        listingViewModel.searchedListings.observe(viewLifecycleOwner) { resource ->
            handleListingsResource(resource)
        }
    }

    private fun handleListingsResource(resource: Resource<List<Listing>>) {
        when (resource) {
            is Resource.Loading -> showLoading(true)
            is Resource.Success -> {
                showLoading(false)
                allListings = resource.data ?: emptyList()
                sortAndDisplayListings(allListings)
            }
            is Resource.Error -> {
                showLoading(false)
                Toast.makeText(context, "Failed to fetch listings: ${resource.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
