package com.example.e_wastehubkenya.ui.theme.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.e_wastehubkenya.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    private val roles = arrayOf("Seller", "Buyer")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupRoleSpinner()

        binding.button.setOnClickListener { handleSignup() }
    }

    private fun setupRoleSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter
    }

    private fun handleSignup() {
        val name = binding.editTextText.text.toString().trim()
        val email = binding.editTextTextEmailAddress.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val password = binding.editTextNumberPassword.text.toString().trim()
        val role = binding.spinner.selectedItem.toString()

        if (!validateFields(name, email, phone, password)) return

        binding.progressBar.isVisible = true
        binding.button.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            binding.progressBar.isVisible = false
            binding.button.isEnabled = true

            if (task.isSuccessful) {
                Toast.makeText(this, "Signup Successful! Please login.", Toast.LENGTH_LONG).show()
                auth.currentUser?.sendEmailVerification()

                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            } else {
                Toast.makeText(this, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateFields(name: String, email: String, phone: String, password: String): Boolean {
        if (name.isEmpty()) { binding.tilName.error = "Name is required"; return false } else { binding.tilName.error = null }
        if (email.isEmpty()) { binding.tilEmail.error = "Email is required"; return false }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { binding.tilEmail.error = "Enter valid email"; return false } else { binding.tilEmail.error = null }
        if (phone.isEmpty()) { binding.tilPhone.error = "Phone required"; return false } else { binding.tilPhone.error = null }
        if (password.isEmpty()) { binding.tilPassword.error = "Password required"; return false }
        else if (password.length < 6) { binding.tilPassword.error = "Password must be at least 6 chars"; return false } else { binding.tilPassword.error = null }
        return true
    }
}
