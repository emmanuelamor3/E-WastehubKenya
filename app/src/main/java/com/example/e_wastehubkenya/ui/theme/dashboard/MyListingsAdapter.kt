package com.example.e_wastehubkenya.ui.theme.dashboard

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.model.Listing
import com.example.e_wastehubkenya.databinding.ItemListingBinding

class MyListingsAdapter(
    private var listings: List<Listing>,
    private val onItemClick: (Listing) -> Unit
) : RecyclerView.Adapter<MyListingsAdapter.ListingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val binding = ItemListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        holder.bind(listings[position])
    }

    override fun getItemCount() = listings.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateListings(newListings: List<Listing>) {
        listings = newListings
        notifyDataSetChanged() // This is a simple approach; for better performance, use DiffUtil
    }

    inner class ListingViewHolder(private val binding: ItemListingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listing: Listing) {
            binding.tvProductName.text = listing.productName
            binding.tvPrice.text = String.format("Ksh %.2f", listing.price)
            val firstImage = listing.imageUrls.firstOrNull()
            if (!firstImage.isNullOrEmpty()) {
                binding.ivProductImage.load(firstImage)
            } else {
                binding.ivProductImage.setImageResource(R.drawable.ic_baseline_image_24)
            }
            binding.root.setOnClickListener {
                onItemClick(listing)
            }
        }
    }
}