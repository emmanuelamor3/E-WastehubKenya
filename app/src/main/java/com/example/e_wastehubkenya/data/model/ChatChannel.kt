package com.example.e_wastehubkenya.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatChannel(
    val channelId: String = "",
    val userIds: List<String> = emptyList(),
    val userNames: Map<String, String> = emptyMap(),
    val userProfilePictures: Map<String, String> = emptyMap(),
    val lastMessage: String? = null,
    @ServerTimestamp
    val lastMessageTimestamp: Date? = null,
    val unreadCount: Map<String, Int> = emptyMap(),
    val listingId: String = "",
    val listingImageUrl: String = "", // Added for product image
    val listingName: String = "" // Added for product name
)
