package com.pab.digitallearning.ui.teacher

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.core.view.WindowCompat

class TeacherDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge support
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Make system bars completely transparent for clean overlap
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        
        // Set light/dark icons for system bars
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false // False = White icons (perfect for dark blue top bar)
        insetsController.isAppearanceLightNavigationBars = true // True = Dark icons (perfect for white bottom nav)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = androidx.compose.ui.graphics.Color(0xFF102B5E),
                    onPrimary = androidx.compose.ui.graphics.Color.White,
                    primaryContainer = androidx.compose.ui.graphics.Color(0xFFF0F5FD),
                    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF102B5E),
                    secondary = androidx.compose.ui.graphics.Color(0xFF1D4ED8),
                    onSecondary = androidx.compose.ui.graphics.Color.White,
                    background = androidx.compose.ui.graphics.Color(0xFFF8FAFD),
                    onBackground = androidx.compose.ui.graphics.Color(0xFF1F2937),
                    surface = androidx.compose.ui.graphics.Color.White,
                    onSurface = androidx.compose.ui.graphics.Color(0xFF1F2937),
                    surfaceVariant = androidx.compose.ui.graphics.Color.White,
                    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF4B5563),
                    surfaceContainer = androidx.compose.ui.graphics.Color.White,
                    surfaceContainerHigh = androidx.compose.ui.graphics.Color.White,
                    outline = androidx.compose.ui.graphics.Color(0xFF9CA3AF)
                )
            ) {
                TeacherMainScreen()
            }
        }
    }
}
