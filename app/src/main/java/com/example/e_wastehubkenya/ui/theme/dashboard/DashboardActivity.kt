package com.example.e_wastehubkenya.ui.theme.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

        // Find the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment

        // Get the NavController
        val navController = navHostFragment.navController

        // Connect the BottomNavigationView to the NavController
        // This single line handles all the click listeners and fragment switching!
        binding.bottomNavView.setupWithNavController(navController)
    }
}