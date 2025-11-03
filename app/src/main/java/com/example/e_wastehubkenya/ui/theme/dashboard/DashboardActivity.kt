package com.example.e_wastehubkenya.ui.theme.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userRole = intent.getStringExtra("USER_ROLE")

        // Find the NavHostFragment and get the NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup BottomNavigationView with the appropriate menu
        if (userRole == "Seller") {
            binding.bottomNavView.menu.clear()
            binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu)
        } else {
            binding.bottomNavView.menu.clear()
            binding.bottomNavView.inflateMenu(R.menu.buyer_bottom_nav_menu)
        }

        binding.bottomNavView.setupWithNavController(navController)
    }
}
