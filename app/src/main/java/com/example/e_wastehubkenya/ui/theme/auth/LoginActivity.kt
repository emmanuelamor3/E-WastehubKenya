package com.example.e_wastehubkenya.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.databinding.ActivityLoginBinding
import com.example.e_wastehubkenya.ui.theme.ThemeManager
import com.example.e_wastehubkenya.ui.theme.dashboard.DashboardActivity
import com.example.e_wastehubkenya.viewmodel.AuthViewModel
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var hasNavigated = false // Prevents multiple navigation attempts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize App Check with the debug provider
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        observeNavigation()
        observeLoginResult()

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.tvGoToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun observeNavigation() {
        authViewModel.userRole.observe(this) { role ->
            // This block is for both auto-login and manual login.
            // It fires when the user's role is successfully determined.
            if (!role.isNullOrEmpty() && !hasNavigated) {
                goToDashboard(role)
            }
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return
        }
        binding.tilEmail.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return
        }
        binding.tilPassword.error = null

        authViewModel.loginUser(email, password)
    }

    private fun observeLoginResult() {
        authViewModel.loginResult.observe(this) { resource ->
            binding.progressBar.isVisible = resource is Resource.Loading
            binding.btnLogin.isEnabled = resource !is Resource.Loading

            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(this, resource.data, Toast.LENGTH_SHORT).show()
                    // Navigation is handled by `observeNavigation` once the role is fetched.
                }
                is Resource.Error -> {
                    Toast.makeText(this, "Login Failed: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> { /* Handled by visibility toggle */ }
            }
        }
    }

    private fun goToDashboard(userRole: String) {
        if (hasNavigated) return // Double-check to prevent re-entry
        hasNavigated = true

        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("USER_ROLE", userRole)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
