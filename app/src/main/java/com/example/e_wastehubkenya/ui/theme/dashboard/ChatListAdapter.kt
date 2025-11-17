package com.example.e_wastehubkenya.ui.theme.dashboard

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.model.ChatChannel
import com.example.e_wastehubkenya.databinding.ItemChatListBinding
import com.google.firebase.auth.FirebaseAuth

class ChatListAdapter(
    private var channels: List<ChatChannel>,
    private val onItemClick: (ChatChannel) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val binding = ItemChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        holder.bind(channels[position])
    }

    override fun getItemCount() = channels.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateChannels(newChannels: List<ChatChannel>) {
        channels = newChannels
        notifyDataSetChanged()
    }

    inner class ChatListViewHolder(private val binding: ItemChatListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(channel: ChatChannel) {
            val otherUserId = channel.userIds.firstOrNull { it != auth.currentUser?.uid }

            if (otherUserId != null) {
                binding.tvUserName.text = channel.userNames[otherUserId]
                binding.tvLastMessage.text = channel.lastMessage
                val profilePicUrl = channel.userProfilePictures[otherUserId]
                if (!profilePicUrl.isNullOrEmpty()) {
                    binding.ivProfileImage.load(profilePicUrl)
                } else {
                    binding.ivProfileImage.setImageResource(R.drawable.ic_baseline_person_24)
                }
            } else {
                // Handle case where other user is not found
                binding.tvUserName.text = "Invalid Chat"
                binding.tvLastMessage.text = ""
                binding.ivProfileImage.setImageResource(R.drawable.ic_baseline_person_24)
            }

            binding.root.setOnClickListener {
                onItemClick(channel)
            }
        }
    }
}
