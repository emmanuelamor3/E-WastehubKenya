package com.example.e_wastehubkenya.ui.theme.dashboard

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.model.CartItem
import com.example.e_wastehubkenya.databinding.ItemCartBinding

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val onDeleteClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount() = cartItems.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateCartItems(newCartItems: List<CartItem>) {
        cartItems = newCartItems
        notifyDataSetChanged()
    }

    inner class CartViewHolder(private val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cartItem: CartItem) {
            binding.tvProductName.text = cartItem.productName
            binding.tvPrice.text = String.format("Ksh %.2f", cartItem.price)
            if (cartItem.imageUrl.isNotEmpty()) {
                binding.ivProductImage.load(cartItem.imageUrl)
            } else {
                binding.ivProductImage.setImageResource(R.drawable.ic_baseline_image_24)
            }
            binding.btnDelete.setOnClickListener {
                onDeleteClick(cartItem)
            }
        }
    }
}
