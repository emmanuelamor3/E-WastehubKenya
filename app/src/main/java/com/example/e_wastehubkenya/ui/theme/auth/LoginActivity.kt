package com.example.e_wastehubkenya.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.e_wastehubkenya.databinding.ActivityLoginBinding
import com.example.e_wastehubkenya.ui.theme.dashboard.DashboardActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewBinding with activity_login.xml
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Login button click
        binding.btnLogin.setOnClickListener { handleLogin() }

        // Go to Signup screen
        binding.tvGoToSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validate inputs
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email required"
            return
        } else { binding.tilEmail.error = null }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password required"
            return
        } else { binding.tilPassword.error = null }

        // Show progress
        binding.progressBar.isVisible = true
        binding.btnLogin.isEnabled = false

        // Firebase login
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.progressBar.isVisible = false
                binding.btnLogin.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
