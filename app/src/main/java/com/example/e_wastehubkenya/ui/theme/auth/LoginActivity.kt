package com.example.e_wastehubkenya.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.databinding.LoginBinding
import com.example.e_wastehubkenya.ui.theme.dashboard.DashboardActivity
import com.example.e_wastehubkenya.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    // 1. View Binding Setup
    private lateinit var binding: LoginBinding

    // 2. ViewModel Initialization
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Set up the observer for login results
        observeLoginResult()

        // 4. Set up click listener for the login button
        binding.btnLogin.setOnClickListener {
            handleLogin()
        }


    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Simple validation
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return
        } else {
            binding.tilPassword.error = null
        }

        // All good, call the ViewModel
        authViewModel.loginUser(email, password)
    }

    private fun observeLoginResult() {
        // Observe the LiveData from the ViewModel
        authViewModel.loginResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show progress bar, disable button
                    binding.progressBar.isVisible = true
                    binding.btnLogin.isEnabled = false
                }
                is Resource.Success -> {
                    // Hide progress bar, enable button
                    binding.progressBar.isVisible = false
                    binding.btnLogin.isEnabled = true

                    // Show success message
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                    // Save the token
                    resource.data?.token?.let { saveAuthToken(it) }

                    // Navigate to Dashboard
                    val intent = Intent(this, DashboardActivity::class.java)
                    // Clear the back stack so user can't press "back" to go to Login
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is Resource.Error -> {
                    // Hide progress bar, enable button
                    binding.progressBar.isVisible = false
                    binding.btnLogin.isEnabled = true

                    // Show error message
                    Toast.makeText(this, "Login Failed: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun saveAuthToken(token: String) {
        // 1. Get or create the master key
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        // 2. Initialize EncryptedSharedPreferences
        val sharedPreferences = EncryptedSharedPreferences.create(
            "auth_prefs", // filename
            masterKeyAlias,
            this, // context
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // 3. Save the token
        with(sharedPreferences.edit()) {
            putString("jwt_token", token)
            apply()
        }
    }
}