package com.example.e_wastehubkenya.ui.theme.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.databinding.FragmentChatListBinding
import com.example.e_wastehubkenya.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var chatListAdapter: ChatListAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        chatViewModel.getChatChannels()
    }

    private fun setupRecyclerView() {
        chatListAdapter = ChatListAdapter(emptyList()) { channel ->
            val currentUserId = auth.currentUser?.uid
            val otherUserId = channel.userIds.find { it != currentUserId }
            val bundle = Bundle().apply {
                putString("sellerId", otherUserId)
                putString("listingId", channel.listingId)
            }
            findNavController().navigate(R.id.action_chatListFragment_to_chatFragment, bundle)
        }
        binding.rvChatList.apply {
            adapter = chatListAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        chatViewModel.chatChannels.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* Show loading */ }
                is Resource.Success -> {
                    chatListAdapter.updateChannels(resource.data ?: emptyList())
                }
                is Resource.Error -> { /* Handle error */ }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
