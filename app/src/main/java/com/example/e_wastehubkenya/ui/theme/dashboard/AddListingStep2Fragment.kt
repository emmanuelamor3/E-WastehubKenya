package com.example.e_wastehubkenya.ui.theme.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.databinding.FragmentAddStep2Binding
import com.example.e_wastehubkenya.viewmodel.AddListingViewModel
import com.example.e_wastehubkenya.viewmodel.ListingViewModel

class AddListingStep2Fragment : Fragment() {

    private var _binding: FragmentAddStep2Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: AddListingViewModel by activityViewModels()
    private val listingViewModel: ListingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.switchDonate.setOnCheckedChangeListener { _, isChecked ->
            binding.tilPrice.isVisible = !isChecked
            binding.tilLocation.isVisible = true
        }

        binding.btnSubmitListing.setOnClickListener {
            handleSubmit()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        listingViewModel.createListingResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, resource.data, Toast.LENGTH_LONG).show()
                    sharedViewModel.clear()
                    findNavController().popBackStack(R.id.my_listings_fragment, false)
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, "Failed to create listing: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleSubmit() {
        val description = binding.etDescription.text.toString().trim()
        val isDonation = binding.switchDonate.isChecked
        val priceText = if (!isDonation) binding.etPrice.text.toString().trim() else "0"
        val location = binding.etLocation.text.toString().trim()

        if (description.isEmpty() || location.isEmpty() || (!isDonation && priceText.isEmpty())) {
            Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val currentListing = sharedViewModel.listingInProgress.value!!
        val imageUris = sharedViewModel.imageUris.value!!

        val finalListing = currentListing.copy(
            description = description,
            price = priceText.toDoubleOrNull() ?: 0.0,
            location = location,
            isDonation = isDonation
        )

        listingViewModel.createListing(finalListing, imageUris)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
