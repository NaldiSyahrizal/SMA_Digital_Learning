package com.pab.digitallearning

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.pab.digitallearning.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Match status bar color to the white header with dark icons for a clean and premium look
        window.statusBarColor = Color.WHITE
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true // True = Dark icons (perfect for white background)
        
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup Bottom Navigation with NavController
        binding.bottomNavigationView.setupWithNavController(navController)
        
        // Set active indicator color (Material 3 pill background) to a premium light blue matching the theme
        binding.bottomNavigationView.itemActiveIndicatorColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#E8EEF9"))

        // Prevent double bottom padding in BottomNavigationView
        binding.bottomNavigationView.setOnApplyWindowInsetsListener { _, insets ->
            insets
        }
    }
}
