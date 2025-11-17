package com.example.e_wastehubkenya.ui.theme.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.model.SoldItem
import com.example.e_wastehubkenya.databinding.ItemMySaleBinding

class MySalesAdapter : ListAdapter<SoldItem, MySalesAdapter.MySalesViewHolder>(SoldItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MySalesViewHolder {
        val binding = ItemMySaleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MySalesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MySalesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MySalesViewHolder(private val binding: ItemMySaleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(soldItem: SoldItem) {
            binding.tvProductName.text = soldItem.productName
            binding.tvPrice.text = String.format("Sold for: Ksh %.2f", soldItem.price)

            if (soldItem.imageUrl.isNotEmpty()) {
                binding.ivProductImage.load(soldItem.imageUrl)
            } else {
                binding.ivProductImage.setImageResource(R.drawable.ic_baseline_image_24)
            }
        }
    }

    class SoldItemDiffCallback : DiffUtil.ItemCallback<SoldItem>() {
        override fun areItemsTheSame(oldItem: SoldItem, newItem: SoldItem): Boolean {
            return oldItem.listingId == newItem.listingId && oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: SoldItem, newItem: SoldItem): Boolean {
            return oldItem == newItem
        }
    }
}
