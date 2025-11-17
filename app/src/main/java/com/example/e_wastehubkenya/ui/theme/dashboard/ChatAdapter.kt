package com.example.e_wastehubkenya.ui.theme.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(
    var messages: List<ChatMessage>,
    var userProfilePictures: Map<String, String>
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == auth.currentUser?.uid) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = if (viewType == VIEW_TYPE_SENT) {
            layoutInflater.inflate(R.layout.item_chat_message_sent, parent, false)
        } else {
            layoutInflater.inflate(R.layout.item_chat_message_received, parent, false)
        }
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)
        private val profileImage: CircleImageView? = itemView.findViewById(R.id.ivProfileImage) // Nullable for sent messages

        fun bind(message: ChatMessage) {
            messageText.text = message.message
            if (profileImage != null) {
                val profilePicUrl = userProfilePictures[message.senderId]
                if (!profilePicUrl.isNullOrEmpty()) {
                    profileImage.load(profilePicUrl)
                } else {
                    profileImage.setImageResource(R.drawable.ic_baseline_person_24) // Placeholder
                }
            }
        }
    }
}