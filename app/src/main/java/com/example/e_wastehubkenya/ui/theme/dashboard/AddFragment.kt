package com.example.e_wastehubkenya.ui.theme.dashboard
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.viewmodel.ListingViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.e_wastehubkenya.databinding.FragmentAddBinding

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    // Get the ViewModel for this fragment
    private val listingViewModel: ListingViewModel by viewModels()

    // This will hold the URI of the image the user selects
    private var selectedImageUri: Uri? = null

    // This is the new way to handle getting content (like an image)
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Show a preview of the selected image
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
        binding.AddFragmentTitle.text = "Add E-Waste"
    }
    /**
     * Populates the category spinner with options
     */
    private fun setupCategorySpinner() {
        // You should move this to a res/values/strings.xml array resource later
        val categories = arrayOf("Laptops", "Phones", "Televisions", "Appliances", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    /**
     * Sets up all the click and change listeners for the form
     */
    private fun setupClickListeners() {
        // Hides/shows the price when the "Donate" switch is toggled
        binding.switchDonate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tilPrice.visibility = View.GONE
                binding.etPrice.setText("0") // Set price to 0 if donating
            } else {
                binding.tilPrice.visibility = View.VISIBLE
                binding.etPrice.setText("")
            }
        }

        // Launches the image picker
        binding.btnUploadImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Calls the ViewModel to check the serial number
        binding.btnCheckSerial.setOnClickListener {
            val serial = binding.etSerial.text.toString().trim()
            if (serial.isEmpty()) {
                binding.tilSerial.error = "Serial number is required to check"
            } else {
                binding.tilSerial.error = null
                listingViewModel.checkSerialNumber(serial)
            }
        }

        // Submits the entire form
        binding.btnSubmitListing.setOnClickListener {
            handleSubmitListing()
        }
    }

    /**
     * Sets up observers on the ViewModel's LiveData
     */
    private fun observeViewModel() {
        // Observer for the serial check API call
        listingViewModel.serialCheckResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    Toast.makeText(context, "Checking serial...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    // API returns 200 OK if NOT found (safe to register)
                    Toast.makeText(context, "✅ Serial number is clear!", Toast.LENGTH_LONG).show()
                    binding.tilSerial.error = null
                }
                is Resource.Error -> {
                    // API returns error if FOUND (stolen)
                    Toast.makeText(context, "⚠️ ${resource.message}", Toast.LENGTH_LONG).show()
                    binding.tilSerial.error = "This item may be flagged"
                }
            }
        }

        // Observer for the create listing API call
        listingViewModel.createListingResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    Toast.makeText(context, "Listing created successfully!", Toast.LENGTH_LONG).show()
                    // TODO: Navigate back to MyListingsFragment or BrowseFragment
                    // findNavController().navigate(R.id.action_addFragment_to_myListingsFragment)
                    clearForm()
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(context, "Failed to create listing: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Validates the form and, if successful, submits it to the ViewModel
     */
    private fun handleSubmitListing() {
        // 1. Get all data from the form
        val productName = binding.etProductName.text.toString().trim()
        val category = binding.spinnerCategory.selectedItem.toString()
        val brand = binding.etBrand.text.toString().trim()
        val model = binding.etModel.text.toString().trim()
        val condition = getSelectedCondition()
        val serial = binding.etSerial.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val price = binding.etPrice.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        // 2. Validate the data
        if (!validateForm(productName, condition, location, price)) {
            Toast.makeText(context, "Please fix the errors on the form.", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Convert all String data to RequestBody
        val productNameBody = productName.toRequestBody("text/plain".toMediaTypeOrNull())
        val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())
        val brandBody = brand.toRequestBody("text/plain".toMediaTypeOrNull())
        val modelBody = model.toRequestBody("text/plain".toMediaTypeOrNull())
        val conditionBody = condition.toRequestBody("text/plain".toMediaTypeOrNull())
        val serialBody = serial.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val priceBody = price.toRequestBody("text/plain".toMediaTypeOrNull())
        val locationBody = location.toRequestBody("text/plain".toMediaTypeOrNull())

        // 4. Convert the image Uri to a MultipartBody.Part
        val imagePart = createImagePart(selectedImageUri!!)

        // 5. Call the ViewModel to submit
        listingViewModel.submitListing(
            productNameBody, categoryBody, brandBody, modelBody, conditionBody,
            serialBody, descriptionBody, priceBody, locationBody, imagePart
        )
    }

    /**
     * Helper function to validate the required fields
     */
    private fun validateForm(name: String, condition: String, location: String, price: String): Boolean {
        var isValid = true
        // Reset errors
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

    /**
     * Gets the selected string from the RadioGroup
     */
    private fun getSelectedCondition(): String {
        return when (binding.rgCondition.checkedRadioButtonId) {
            R.id.rbWorking -> "Working"
            R.idid.rbBroken -> "Broken"
            R.id.rbScrap -> "Scrap"
            else -> ""
        }
    }

    /**
     * Converts a file Uri into a MultipartBody.Part for file upload
     */
    private fun createImagePart(imageUri: Uri): MultipartBody.Part {
        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(imageUri)!!

        // Get file mime type (e.g., "image/jpeg") and create a file name
        val mimeType = contentResolver.getType(imageUri)
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
        val fileName = "image_${System.currentTimeMillis()}.$extension"

        // Create RequestBody from the file's bytes
        val requestFile = inputStream.readBytes()
            .toRequestBody(mimeType?.toMediaTypeOrNull())

        // Create the final MultipartBody.Part
        return MultipartBody.Part.createFormData("image", fileName, requestFile)
    }

    /**
     * Toggles the loading spinner and button state
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.btnSubmitListing.isEnabled = !isLoading
    }

    /**
     * Clears all fields after a successful submission
     */
    private fun clearForm() {
        binding.etProductName.setText("")
        binding.etBrand.setText("")
        binding.etModel.setText("")
        binding.etSerial.setText("")
        binding.etDescription.setText("")
        binding.etPrice.setText("")
        binding.etLocation.setText("")
        binding.rgCondition.clearCheck()
        binding.switchDonate.isChecked = false
        binding.ivProductImage.setImageResource(R.drawable.ic_baseline_image_24) // Reset to placeholder
        selectedImageUri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


