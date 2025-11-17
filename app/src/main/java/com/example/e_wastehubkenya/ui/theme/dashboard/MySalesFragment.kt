package com.example.e_wastehubkenya.ui.theme.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.SoldItem
import com.example.e_wastehubkenya.databinding.FragmentMySalesBinding
import com.example.e_wastehubkenya.viewmodel.OrderViewModel

class MySalesFragment : Fragment() {

    private var _binding: FragmentMySalesBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by viewModels()
    private lateinit var mySalesAdapter: MySalesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMySalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        orderViewModel.fetchMySales()
    }

    private fun setupRecyclerView() {
        mySalesAdapter = MySalesAdapter()
        binding.rvMySales.apply {
            adapter = mySalesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        orderViewModel.mySales.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    val soldItems = resource.data?.flatMap { order ->
                        order.items.map { cartItem ->
                            SoldItem(
                                listingId = cartItem.listingId,
                                productName = cartItem.productName,
                                price = cartItem.price,
                                imageUrl = cartItem.imageUrl,
                                orderId = order.orderId,
                                purchaseDate = order.timestamp
                            )
                        }
                    } ?: emptyList()
                    mySalesAdapter.submitList(soldItems)
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
