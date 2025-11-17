package com.example.e_wastehubkenya.ui.theme.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.databinding.FragmentCartBinding
import com.example.e_wastehubkenya.viewmodel.CartViewModel

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        cartViewModel.fetchCartItems()

        binding.btnCheckout.setOnClickListener {
            findNavController().navigate(R.id.action_cartFragment_to_checkoutFragment)
        }

        binding.btnContinueShopping.setOnClickListener {
            findNavController().navigate(R.id.my_listings_fragment)
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(emptyList()) { cartItem ->
            cartViewModel.removeFromCart(cartItem)
        }
        binding.rvCartItems.apply {
            adapter = cartAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    val cartItems = resource.data ?: emptyList()
                    binding.btnCheckout.isEnabled = cartItems.isNotEmpty()
                    if (cartItems.isEmpty()) {
                        binding.btnCheckout.text = "Cart is empty"
                    } else {
                        binding.btnCheckout.text = "Checkout"
                    }
                    cartAdapter.updateCartItems(cartItems)
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        cartViewModel.removeFromCartResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* Optionally show a small loader */ }
                is Resource.Success -> {
                    Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show()
                    // The cartItems observer will automatically handle the UI update
                }
                is Resource.Error -> {
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
