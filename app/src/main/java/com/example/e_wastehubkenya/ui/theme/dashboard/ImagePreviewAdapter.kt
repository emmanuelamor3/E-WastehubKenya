package com.example.e_wastehubkenya.ui.theme.dashboard

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.e_wastehubkenya.databinding.ItemImagePreviewBinding

class ImagePreviewAdapter(private val images: MutableList<Uri>) : RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImagePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size

    fun addImages(uris: List<Uri>) {
        images.addAll(uris)
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(private val binding: ItemImagePreviewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            binding.ivPreview.load(uri)
        }
    }
}