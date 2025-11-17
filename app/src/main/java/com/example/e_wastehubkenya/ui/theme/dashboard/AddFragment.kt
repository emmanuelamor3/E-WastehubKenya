package com.example.e_wastehubkenya.ui.theme.dashboard

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.databinding.FragmentAddBinding
import com.example.e_wastehubkenya.viewmodel.AddListingViewModel
import com.example.e_wastehubkenya.viewmodel.SerialNumberViewModel

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: AddListingViewModel by activityViewModels()
    private val serialNumberViewModel: SerialNumberViewModel by viewModels()
    private var isSerialNumberVerified = false

    private val selectedImageUris = mutableListOf<Uri>()
    private lateinit var imagePreviewAdapter: ImagePreviewAdapter

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (selectedImageUris.size + uris.size > 5) {
            Toast.makeText(context, "You can select a maximum of 5 images.", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        imagePreviewAdapter.addImages(uris)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupClickListeners()
        setupImagePreview()
        observeViewModel()
        binding.addFragmentTitle.text = getString(R.string.add_e_waste_title)
    }

    private fun observeViewModel() {
        serialNumberViewModel.verificationResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnVerify.isEnabled = false
                    binding.tilSerialNumber.error = null
                }
                is Resource.Success -> {
                    binding.btnVerify.isEnabled = true
                    isSerialNumberVerified = resource.data ?: false
                    if (isSerialNumberVerified) {
                        Toast.makeText(context, "Serial number is valid", Toast.LENGTH_SHORT).show()
                        binding.tilSerialNumber.error = null
                    } 
                }
                is Resource.Error -> {
                    binding.btnVerify.isEnabled = true
                    isSerialNumberVerified = false
                    binding.tilSerialNumber.error = resource.message
                }
            }
        }
    }

    private fun setupImagePreview() {
        imagePreviewAdapter = ImagePreviewAdapter(selectedImageUris)
        binding.rvImagePreviews.adapter = imagePreviewAdapter
    }

    private fun setupCategorySpinner() {
        val categories = resources.getStringArray(R.array.categories_array)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnAddPhotos.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnVerify.setOnClickListener {
            val serialNumber = binding.etSerialNumber.text.toString().trim()
            if (serialNumber.isNotEmpty()) {
                serialNumberViewModel.verifySerialNumber(serialNumber)
            } else {
                Toast.makeText(context, "Please enter a serial number", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnNext.setOnClickListener {
            if (validateStep1()) {
                saveStep1Data()
                findNavController().navigate(R.id.action_AddFragment_to_addListingStep2Fragment)
            }
        }
    }

    private fun validateStep1(): Boolean {
        // Basic validation
        if (binding.etProductName.text.isNullOrEmpty()) {
            Toast.makeText(context, "Product name is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(context, "Please add at least one photo", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!isSerialNumberVerified) {
            Toast.makeText(context, "Please verify the serial number", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveStep1Data() {
        val currentListing = sharedViewModel.listingInProgress.value!!
        val updatedListing = currentListing.copy(
            productName = binding.etProductName.text.toString(),
            category = binding.spinnerCategory.selectedItem.toString(),
            brand = binding.etBrand.text.toString(),
            model = binding.etModel.text.toString(),
            serialNumber = binding.etSerialNumber.text.toString(),
            condition = getSelectedCondition()
        )
        sharedViewModel.updatePartialListing(updatedListing)
        sharedViewModel.updateImageUris(selectedImageUris)
    }

    private fun getSelectedCondition(): String {
        return when (binding.rgCondition.checkedRadioButtonId) {
            R.id.rbWorking -> "Working"
            R.id.rbBroken -> "Broken"
            else -> ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
