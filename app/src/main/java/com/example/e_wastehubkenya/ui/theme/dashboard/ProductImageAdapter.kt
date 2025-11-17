package com.example.e_wastehubkenya.ui.theme.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.e_wastehubkenya.databinding.ItemProductImageBinding

class ProductImageAdapter(private val images: List<String>) : RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemProductImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size

    inner class ImageViewHolder(private val binding: ItemProductImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUrl: String) {
            binding.ivProductImage.load(imageUrl)
        }
    }
}