package com.example.e_wastehubkenya.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.local.AuthPreference
import com.example.e_wastehubkenya.databinding.LoginBinding
import com.example.e_wastehubkenya.ui.theme.dashboard.DashboardActivity
import com.example.e_wastehubkenya.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var authPreference: AuthPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authPreference = AuthPreference(this)
        observeLoginResult()

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val selectedRoleId = binding.rgRole.checkedRadioButtonId

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

        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
            return
        }

        val role = findViewById<RadioButton>(selectedRoleId).text.toString()

        authViewModel.loginUser(email, password, role)
    }

    private fun observeLoginResult() {
        authViewModel.loginResult.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.btnLogin.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    binding.btnLogin.isEnabled = true

                    if (resource.data?.token != null) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                        resource.data.token?.let { authPreference.saveToken(it) }
                        val role = resource.data.role

                        val intent = Intent(this, DashboardActivity::class.java).apply {
                            putExtra("USER_ROLE", role)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Login Failed: ${resource.data?.message}", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Login Failed: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
