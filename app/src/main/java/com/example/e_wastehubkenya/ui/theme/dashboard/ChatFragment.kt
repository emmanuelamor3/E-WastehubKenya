package com.example.e_wastehubkenya.ui.theme.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.ChatMessage
import com.example.e_wastehubkenya.data.model.User
import com.example.e_wastehubkenya.databinding.FragmentChatBinding
import com.example.e_wastehubkenya.viewmodel.ChatViewModel
import com.example.e_wastehubkenya.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var auth: FirebaseAuth
    private var channelId: String? = null
    private var otherUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val sellerId = arguments?.getString("sellerId")
        val listingId = arguments?.getString("listingId")

        if (sellerId != null && listingId != null) {
            chatViewModel.getOrCreateChatChannel(sellerId, listingId)
        } else {
            // Handle the case where the arguments are null
        }

        userViewModel.fetchUser() // Fetch current user details

        setupRecyclerView()
        observeViewModel()

        binding.btnSend.setOnClickListener {
            sendMessage(binding.etMessage.text.toString().trim())
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(emptyList(), emptyMap())
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun observeViewModel() {
        chatViewModel.channelId.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* Show loading */ }
                is Resource.Success -> {
                    channelId = resource.data
                    channelId?.let {
                        chatViewModel.getMessages(it)
                        chatViewModel.markMessagesAsRead(it)
                        chatViewModel.getChannelDetails(it)
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        chatViewModel.channelDetails.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                val channel = resource.data
                val currentUserId = auth.currentUser?.uid
                if (channel != null && currentUserId != null) {
                    otherUserId = channel.userIds.find { it != currentUserId }
                    otherUserId?.let { userViewModel.fetchUserById(it) }

                    // Load listing info into the toolbar
                    binding.tvToolbarListingName.text = channel.listingName
                    if (channel.listingImageUrl.isNotEmpty()) {
                        binding.ivToolbarListingImage.load(channel.listingImageUrl)
                    }
                }
            }
        }

        val messagesObserver = Observer<Resource<List<ChatMessage>>> { resource ->
            if (resource is Resource.Success) {
                val messages = resource.data ?: emptyList()
                updateChatAdapter(messages)
            }
        }

        val userObserver = Observer<Resource<User>> { updateUserAdapter() }
        val sellerObserver = Observer<Resource<User>> { resource ->
            if (resource is Resource.Success) {
                val otherUser = resource.data
                binding.tvToolbarUserName.text = otherUser?.name
                if (otherUser?.profilePictureUrl?.isNotEmpty() == true) {
                    binding.ivToolbarProfileImage.load(otherUser.profilePictureUrl)
                }
                updateUserAdapter()
            }
        }

        chatViewModel.messages.observe(viewLifecycleOwner, messagesObserver)
        userViewModel.user.observe(viewLifecycleOwner, userObserver)
        userViewModel.seller.observe(viewLifecycleOwner, sellerObserver)
    }

    private fun updateUserAdapter() {
        val messages = chatViewModel.messages.value?.data ?: emptyList()
        updateChatAdapter(messages)
    }

    private fun updateChatAdapter(messages: List<ChatMessage>) {
        val userProfilePictures = getProfilePicturesMap()
        chatAdapter.messages = messages
        chatAdapter.userProfilePictures = userProfilePictures
        chatAdapter.notifyDataSetChanged()
    }


    private fun getProfilePicturesMap(): Map<String, String> {
        val currentUser = userViewModel.user.value?.data
        val otherUser = userViewModel.seller.value?.data
        val profilePictures = mutableMapOf<String, String>()
        currentUser?.let { profilePictures[it.uid] = it.profilePictureUrl }
        otherUser?.let { profilePictures[it.uid] = it.profilePictureUrl }
        return profilePictures
    }

    private fun sendMessage(messageText: String) {
        if (messageText.isNotEmpty()) {
            val senderId = auth.currentUser?.uid ?: return
            val receiverId = otherUserId ?: return
            channelId?.let {
                val message = ChatMessage(
                    senderId = senderId,
                    receiverId = receiverId,
                    message = messageText
                )
                chatViewModel.sendMessage(it, message)
                binding.etMessage.text.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
