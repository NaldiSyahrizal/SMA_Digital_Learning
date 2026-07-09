package com.pab.digitallearning.ui.student.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BadgePriority(sawScore: Double, modifier: Modifier = Modifier) {
    val (label, gradient) = when {
        sawScore >= 85.0 -> {
            "CRITICAL" to Brush.horizontalGradient(
                colors = listOf(Color(0xFFFF3B30), Color(0xFFFF2D55)) // Vibrant Red-Pink
            )
        }
        sawScore >= 70.0 -> {
            "TINGGI" to Brush.horizontalGradient(
                colors = listOf(Color(0xFFFF9500), Color(0xFFFFCC00)) // Orange-Yellow
            )
        }
        sawScore >= 50.0 -> {
            "SEDANG" to Brush.horizontalGradient(
                colors = listOf(Color(0xFF007AFF), Color(0xFF5AC8FA)) // Classic iOS Blue
            )
        }
        else -> {
            "RENDAH" to Brush.horizontalGradient(
                colors = listOf(Color(0xFF34C759), Color(0xFF4CD964)) // Fresh Green
            )
        }
    }

    Box(
        modifier = modifier
            .background(brush = gradient, shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
