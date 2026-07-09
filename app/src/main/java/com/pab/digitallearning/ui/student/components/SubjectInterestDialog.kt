package com.pab.digitallearning.ui.student.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubjectInterestDialog(
    subjectName: String,
    initialInterest: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var interestScore by remember { mutableStateOf(initialInterest.coerceIn(1, 5)) }

    val interestLabels = listOf(
        "Sangat Tidak Minat",
        "Kurang Minat",
        "Cukup Minat",
        "Minat",
        "Sangat Minat"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Skala Minat Belajar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF102B5E),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subjectName,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seberapa berminat Anda mempelajari mata pelajaran ini?",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Five Stars Rating Selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val starValue = index + 1
                        val active = starValue <= interestScore
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = interestLabels[index],
                            tint = if (active) Color(0xFFFFCC00) else Color(0xFFD1D1D6),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    interestScore = starValue
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text Description based on score
                Text(
                    text = interestLabels[interestScore - 1],
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (interestScore) {
                        1 -> Color(0xFFFF3B30) // Red
                        2 -> Color(0xFFFF9500) // Orange
                        3 -> Color(0xFF007AFF) // Blue
                        4 -> Color(0xFF5856D6) // Purple
                        else -> Color(0xFF34C759) // Green
                    },
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(interestScore) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF102B5E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Simpan Pilihan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Batal",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    )
}
