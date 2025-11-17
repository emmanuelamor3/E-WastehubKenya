package com.example.e_wastehubkenya.ui.theme.dashboard

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.databinding.FragmentProfileBinding
import com.example.e_wastehubkenya.ui.theme.auth.LoginActivity
import com.example.e_wastehubkenya.viewmodel.AuthViewModel
import com.example.e_wastehubkenya.viewmodel.UserViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var isEditMode = false

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            imageUri?.let { userViewModel.uploadProfilePicture(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        userViewModel.fetchUser()

        binding.btnChangePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        binding.btnEditSave.setOnClickListener {
            isEditMode = !isEditMode
            toggleEditMode()
            if (!isEditMode) {
                saveChanges()
            }
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            startActivity(Intent(activity, LoginActivity::class.java))
            activity?.finish()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun observeViewModel() {
        userViewModel.user.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.isVisible = true
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    val user = resource.data
                    binding.etName.setText(user?.name)
                    binding.etEmail.setText(user?.email)
                    binding.etPhoneNumber.setText(user?.phoneNumber)
                    if (!user?.profilePictureUrl.isNullOrEmpty()) {
                        binding.ivProfileImage.load(user?.profilePictureUrl)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        userViewModel.updateUserResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.isVisible = true
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, "Update failed: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        userViewModel.uploadProfilePictureResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.isVisible = true
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    val newImageUrl = resource.data
                    binding.ivProfileImage.load(newImageUrl)
                    Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, "Upload failed: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        authViewModel.changePasswordResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.isVisible = true
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, resource.data, Toast.LENGTH_LONG).show()
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    if (resource.message?.contains("requires recent authentication") == true) {
                        showReauthenticationDialog()
                    } else {
                        Toast.makeText(context, "Failed to change password: ${resource.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        authViewModel.deleteAccountResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.isVisible = true
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_LONG).show()
                    authViewModel.logout()
                    startActivity(Intent(activity, LoginActivity::class.java))
                    activity?.finish()
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    if (resource.message?.contains("requires recent authentication") == true) {
                        showReauthenticationDialog()
                    } else {
                        Toast.makeText(context, "Failed to delete account: ${resource.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun toggleEditMode() {
        binding.etName.isEnabled = isEditMode
        binding.etPhoneNumber.isEnabled = isEditMode
        binding.btnEditSave.text = if (isEditMode) "Save" else "Edit Profile"
    }

    private fun saveChanges() {
        val newName = binding.etName.text.toString().trim()
        val newPhoneNumber = binding.etPhoneNumber.text.toString().trim()
        userViewModel.updateUser(newName, newPhoneNumber)
    }

    private fun showChangePasswordDialog() {
        val currentPasswordEditText = EditText(requireContext()).apply {
            hint = "Current Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val newPasswordEditText = EditText(requireContext()).apply {
            hint = "New Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            addView(currentPasswordEditText)
            addView(newPasswordEditText)
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = currentPasswordEditText.text.toString()
                val newPassword = newPasswordEditText.text.toString()

                if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                    authViewModel.changePassword(currentPassword, newPassword)
                } else {
                    Toast.makeText(context, "Passwords cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This will permanently delete all your data and cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                authViewModel.deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showReauthenticationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Authentication Required")
            .setMessage("This is a sensitive operation and requires you to have logged in recently. Please log out and log back in to continue.")
            .setPositiveButton("Log Out") { _, _ ->
                authViewModel.logout()
                startActivity(Intent(activity, LoginActivity::class.java))
                activity?.finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
