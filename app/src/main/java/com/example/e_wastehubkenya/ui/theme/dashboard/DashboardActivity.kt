package com.example.e_wastehubkenya.ui.theme.dashboard

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.databinding.ActivityDashboardBinding
import com.example.e_wastehubkenya.ui.theme.ThemeManager
import com.example.e_wastehubkenya.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applyTheme(this)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Determine which menu to inflate based on user role
        val userRole = intent.getStringExtra("USER_ROLE")
        val menuResId = if (userRole == "Buyer") {
            R.menu.buyer_bottom_nav_menu
        } else {
            R.menu.bottom_nav_menu
        }
        binding.bottomNavView.menu.clear()
        binding.bottomNavView.inflateMenu(menuResId)

        binding.bottomNavView.setupWithNavController(navController)

        binding.bottomNavView.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(item, navController)
            true
        }

        observeChatChannels()
        chatViewModel.getChatChannels()
    }

    private fun observeChatChannels() {
        chatViewModel.chatChannels.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> { /* No badge for loading */ }
                is Resource.Success -> {
                    val unreadChannels = resource.data?.filter { (it.unreadCount[auth.currentUser?.uid] ?: 0) > 0 }
                    if (!unreadChannels.isNullOrEmpty()) {
                        val chatBadge = binding.bottomNavView.getOrCreateBadge(R.id.chat_list_fragment)
                        chatBadge.isVisible = true
                        chatBadge.number = unreadChannels.size
                    } else {
                        binding.bottomNavView.removeBadge(R.id.chat_list_fragment)
                    }
                }
                is Resource.Error -> { /* No badge for error */ }
            }
        }
    }
}
