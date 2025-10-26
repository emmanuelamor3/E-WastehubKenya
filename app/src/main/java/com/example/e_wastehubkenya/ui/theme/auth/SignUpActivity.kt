package com.example.ewastehubkenya.ui.theme.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.databinding.ActivitySignupBinding
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.viewmodel.AuthViewModel

class SignupActivity : AppCompatActivity() {

    // The binding class is generated from XML file name
    private lateinit var binding: ActivitySignupBinding
    private val authViewModel: AuthViewModel by viewModels()

    // Define the roles
    private val roles = arrayOf("Seller", "Buyer")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use the new binding class
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRoleSpinner()
        observeSignupResult()

        // --- ID CHANGED ---
        binding.button.setOnClickListener { // Was btnSignup
            handleSignup()
        }

        // Note: Your new XML doesn't have a "Go to Login" button.
        // You should add one, otherwise users can't go back!
        // binding.tvGoToLogin.setOnClickListener {
        //     finish()
        // }
    }

    private fun setupRoleSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // --- ID CHANGED ---
        binding.spinner.adapter = adapter // Was spinnerRole
    }

    private fun handleSignup() {
        // Get all data from fields (using your new IDs)
        // --- IDS CHANGED ---
        val name = binding.editTextText.text.toString().trim()
        val email = binding.editTextTextEmailAddress.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val password = binding.editTextNumberPassword.text.toString().trim()
        val role = binding.spinner.selectedItem.toString()

        // --- Validation ---
        if (!validateFields(name, email, phone, password)) {
            return // Stop if validation fails
        }

        // --- Call ViewModel ---
        authViewModel.signupUser(name, email, phone, password, role)
    }

    private fun validateFields(name: String, email: String, phone: String, password: String): Boolean {
        // This part is the same, but it now controls the
        // TextInputLayouts (tilName, tilEmail) we added to your XML.
        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            return false
        } else {
            binding.tilName.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email"
            return false
        } else {
            binding.tilEmail.error = null
        }

        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone number is required"
            return false
        } else {
            binding.tilPhone.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return false
        } else {
            binding.tilPassword.error = null
        }

        return true // All fields are valid
    }

    private fun observeSignupResult() {
        authViewModel.signupResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // --- IDS CHANGED ---
                    binding.progressBar.isVisible = true // This ID was added
                    binding.button.isEnabled = false // Was btnSignup
                }
                is Resource.Success -> {
                    // --- IDS CHANGED ---
                    binding.progressBar.isVisible = false
                    binding.button.isEnabled = true

                    Toast.makeText(this, "Signup Successful! Please login.", Toast.LENGTH_LONG).show()
                    finish() // Close this activity and go back to Login
                }
                is Resource.Error -> {
                    // --- IDS CHANGED ---
                    binding.progressBar.isVisible = false
                    binding.button.isEnabled = true

                    Toast.makeText(this, "Signup Failed: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}