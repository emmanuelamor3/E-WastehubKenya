package com.example.e_wastehubkenya.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.model.User
import com.example.e_wastehubkenya.databinding.ActivitySignupBinding
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.ui.theme.ThemeManager
import com.example.e_wastehubkenya.ui.theme.dashboard.DashboardActivity
import com.example.e_wastehubkenya.viewmodel.AuthViewModel

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val roles = arrayOf("Seller", "Buyer")
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeAutoLogin()
        setupRoleSpinner()
        observeSignupResult()

        binding.button.setOnClickListener { // Corrected from btnSignup
            handleSignup()
        }

        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun observeAutoLogin() {
        authViewModel.userRole.observe(this) { role ->
            if (!role.isNullOrEmpty() && !hasNavigated) {
                goToDashboard(role)
            }
        }
    }

    private fun goToDashboard(userRole: String) {
        if (hasNavigated) return
        hasNavigated = true

        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("USER_ROLE", userRole)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupRoleSpinner() {
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_item, roles)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.spinner.adapter = adapter
    }

    private fun handleSignup() {
        val name = binding.editTextText.text.toString().trim()
        val email = binding.editTextTextEmailAddress.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val password = binding.editTextNumberPassword.text.toString().trim()
        val role = binding.spinner.selectedItem.toString()

        if (validateFields(name, email, phone, password)) {
            val user = User(name = name, email = email, phoneNumber = phone, role = role)
            authViewModel.registerUser(user, password)
        }
    }

    private fun validateFields(name: String, email: String, phone: String, password: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            isValid = false
        } else {
            binding.tilName.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone number is required"
            isValid = false
        } else {
            binding.tilPhone.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun observeSignupResult() {
        authViewModel.registerResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.button.isEnabled = false // Corrected from btnSignup
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    binding.button.isEnabled = true // Corrected from btnSignup

                    Toast.makeText(this, "Signup Successful! Please login.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.button.isEnabled = true // Corrected from btnSignup

                    Toast.makeText(this, "Signup Failed: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
