
package com.example.e_wastehubkenya.ui.theme.dashboard

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.databinding.FragmentAddBinding
import com.example.e_wastehubkenya.viewmodel.ListingViewModel
import com.example.e_wastehubkenya.viewmodel.ViewModelFactory
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private val listingViewModel: ListingViewModel by viewModels { ViewModelFactory(requireContext()) }

    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProductImage.setImageURI(it)
        }
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
        observeViewModel()
        binding.addFragmentTitle.text = getString(R.string.add_e_waste_title)
    }

    private fun setupCategorySpinner() {
        val categories = resources.getStringArray(R.array.categories_array)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.switchDonate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tilPrice.visibility = View.GONE
                binding.etPrice.setText("0")
            } else {
                binding.tilPrice.visibility = View.VISIBLE
                binding.etPrice.setText("")
            }
        }

        binding.btnUploadImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSubmit.setOnClickListener {
            handleSubmitListing()
        }
    }

    private fun observeViewModel() {
        listingViewModel.createListingResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(context, "Listing created successfully!", Toast.LENGTH_LONG).show()
                    clearForm()
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(context, "Failed to create listing: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleSubmitListing() {
        val productName = binding.etProductName.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val brand = binding.etBrand.text.toString().trim()
        val model = binding.etModel.text.toString().trim()
        val condition = getSelectedCondition()
        val description = binding.etDescription.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        if (!validateForm(productName, condition, location, price)) {
            Toast.makeText(context, "Please fix the errors on the form.", Toast.LENGTH_SHORT).show()
            return
        }

        val parts = mutableMapOf<String, RequestBody>()
        parts["product_name"] = productName.toRequestBody("text/plain".toMediaTypeOrNull())
        parts["category"] = category.toRequestBody("text/plain".toMediaTypeOrNull())
        parts["brand"] = brand.toRequestBody("text/plain".toMediaTypeOrNull())
        parts["model"] = model.toRequestBody("text/plain".toMediaTypeOrNull())
        parts["condition"] = condition.toRequestBody("text/plain".toMediaTypeOrNull())
        parts["description"] = description.toRequestBody("text/plain".toMediaTypeOrNull())
        parts["price"] = price.toRequestBody("text/plain".toMediaTypeOrNull())
        parts["location"] = location.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = selectedImageUri?.let { createImagePart(it) }

        if (imagePart != null) {
            listingViewModel.submitListing(parts, imagePart)
        } else {
            Toast.makeText(context, "Please upload an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateForm(name: String, condition: String, location: String, price: String): Boolean {
        var isValid = true
        binding.tilProductName.error = null
        binding.tilLocation.error = null
        binding.tilPrice.error = null

        if (name.isEmpty()) {
            binding.tilProductName.error = "Product name is required"
            isValid = false
        }
        if (location.isEmpty()) {
            binding.tilLocation.error = "Location is required"
            isValid = false
        }
        if (price.isEmpty()) {
            binding.tilPrice.error = "Price is required (enter 0 if donating)"
            isValid = false
        }
        if (condition.isEmpty()) {
            Toast.makeText(context, "Please select a condition", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        if (selectedImageUri == null) {
            Toast.makeText(context, "Please upload an image", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        return isValid
    }

    private fun getSelectedCondition(): String {
        return when (binding.rgCondition.checkedRadioButtonId) {
            R.id.rbWorking -> "Working"
            R.id.rbBroken -> "Broken"
            else -> ""
        }
    }

    private fun createImagePart(imageUri: Uri): MultipartBody.Part {
        val contentResolver = requireContext().contentResolver
        val mimeType = contentResolver.getType(imageUri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
        val fileName = "image_${System.currentTimeMillis()}.$extension"

        val requestFile = object : RequestBody() {
            override fun contentType(): MediaType? = mimeType?.toMediaTypeOrNull()

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                val inputStream = contentResolver.openInputStream(imageUri)
                inputStream?.use { input ->
                    sink.writeAll(input.source())
                }
            }
        }

        return MultipartBody.Part.createFormData("image", fileName, requestFile)
    }


    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnSubmit.isEnabled = !isLoading
    }

    private fun clearForm() {
        binding.etProductName.setText("")
        binding.etBrand.setText("")
        binding.etModel.setText("")
        binding.etDescription.setText("")
        binding.etPrice.setText("")
        binding.etLocation.setText("")
        binding.rgCondition.clearCheck()
        binding.switchDonate.isChecked = false
        binding.ivProductImage.setImageResource(R.drawable.ic_baseline_image_24)
        selectedImageUri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
