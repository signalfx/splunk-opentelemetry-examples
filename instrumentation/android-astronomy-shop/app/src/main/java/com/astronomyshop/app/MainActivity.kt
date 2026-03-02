package com.astronomyshop.app

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.astronomyshop.app.databinding.ActivityMainBinding
import com.astronomyshop.app.ui.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        observeViewModel()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> navController.navigate(R.id.navigation_home)
                R.id.navigation_products -> navController.navigate(R.id.navigation_products)
                R.id.navigation_profile -> navController.navigate(R.id.navigation_profile)
                R.id.navigation_orders -> navController.navigate(R.id.navigation_orders)
                R.id.navigation_cart -> navController.navigate(R.id.navigation_cart)
            }
            true
        }

    }

    private fun observeViewModel() {
        viewModel.cartItemCount.observe(this) { count ->
            updateCartBadge(count)
        }
    }

    private fun updateCartBadge(count: Int) {
        val badge = binding.bottomNavigation.getOrCreateBadge(R.id.navigation_cart)
        if (count > 0) {
            badge.isVisible = true
            badge.number = count
        } else {
            badge.isVisible = false
        }
    }
}
