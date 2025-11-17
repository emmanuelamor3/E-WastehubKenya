package com.example.e_wastehubkenya.ui.theme.dashboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.CartItem
import com.example.e_wastehubkenya.databinding.FragmentCheckoutBinding
import com.example.e_wastehubkenya.viewmodel.CartViewModel
import com.example.e_wastehubkenya.viewmodel.MpesaViewModel
import com.example.e_wastehubkenya.viewmodel.OrderViewModel

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by viewModels()
    private val mpesaViewModel: MpesaViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter
    private var cartItems: List<CartItem> = emptyList()
    private var total: Double = 0.0
    private var checkoutRequestID: String? = null

    // Polling variables
    private var pollingCount = 0
    private val maxPollingAttempts = 4
    private val initialPollingDelay = 15000L // 15 seconds
    private val subsequentPollingDelay = 5000L // 5 seconds
    private val pollingHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        observeViewModel()
        cartViewModel.fetchCartItems()

        binding.btnPay.setOnClickListener {
            showPhoneNumberDialog()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(emptyList()) { /* No-op for checkout */ }
        binding.rvOrderItems.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.isVisible = true
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    cartItems = resource.data ?: emptyList()
                    cartAdapter.updateCartItems(cartItems)
                    total = cartItems.sumOf { it.price }
                    binding.tvTotalAmount.text = String.format("Ksh %.2f", total)
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        mpesaViewModel.stkPushResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.btnPay.isEnabled = false
                }
                is Resource.Success -> {
                    Toast.makeText(context, "STK Push sent. Please enter PIN.", Toast.LENGTH_LONG).show()
                    checkoutRequestID = resource.data?.checkoutRequestID
                    pollingCount = 0 // Reset polling count
                    pollPaymentStatus() // Start polling
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.btnPay.isEnabled = true
                    Toast.makeText(context, "Failed to initiate payment: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        mpesaViewModel.stkQueryResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* Polling in progress */ }
                is Resource.Success -> {
                    val queryResponse = resource.data
                    if (queryResponse?.resultCode == "0") {
                        pollingCount = 0
                        Toast.makeText(context, "Payment confirmed!", Toast.LENGTH_SHORT).show()
                        orderViewModel.createOrder(cartItems, total)
                    } else {
                        if (pollingCount < maxPollingAttempts) {
                            // If not successful, poll again after a delay
                            pollPaymentStatus()
                        } else {
                            // Polling attempts exhausted
                            pollingCount = 0
                            binding.progressBar.isVisible = false
                            binding.btnPay.isEnabled = true
                            Toast.makeText(context, "Payment failed or timed out: ${queryResponse?.resultDesc}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                is Resource.Error -> {
                    pollingCount = 0
                    binding.progressBar.isVisible = false
                    binding.btnPay.isEnabled = true
                    Toast.makeText(context, "Payment verification failed: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        orderViewModel.createOrderResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* Handled by STK push loading */ }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, resource.data, Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_checkoutFragment_to_purchaseSuccessfulFragment)
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.btnPay.isEnabled = true
                    Toast.makeText(context, "Failed to create order: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun pollPaymentStatus() {
        pollingCount++
        val delay = if (pollingCount == 1) initialPollingDelay else subsequentPollingDelay

        if (pollingCount == 1) {
            Toast.makeText(context, "Verifying payment...", Toast.LENGTH_SHORT).show()
        }

        pollingHandler.postDelayed({
            checkoutRequestID?.let {
                mpesaViewModel.queryStkPushStatus(it)
            }
        }, delay)
    }

    private fun showPhoneNumberDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "254..."
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Phone Number")
            .setView(editText)
            .setPositiveButton("Pay") { _, _ ->
                val phoneNumber = editText.text.toString()
                if (phoneNumber.length == 12 && phoneNumber.startsWith("254")) {
                    mpesaViewModel.initiateStkPush(
                        amount = "1", // Use "1" for sandbox testing
                        phoneNumber = phoneNumber,
                        accountReference = "E-Waste Hub"
                    )
                } else {
                    Toast.makeText(context, "Please enter a valid phone number (e.g., 254...)", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pollingHandler.removeCallbacksAndMessages(null) // Stop any pending polling
        _binding = null
    }
}