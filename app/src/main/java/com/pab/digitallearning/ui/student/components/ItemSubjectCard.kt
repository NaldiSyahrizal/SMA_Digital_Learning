package com.pab.digitallearning.ui.student.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pab.digitallearning.data.model.StudentSubject

@Composable
fun ItemSubjectCard(
    subject: StudentSubject,
    onClick: () -> Unit,
    onInterestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = if (subject.kategori.lowercase() == "pilihan") {
        Color(0xFFFF9500) // Orange
    } else {
        Color(0xFF34C759) // Green
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { 
                if (subject.interestScore == 0) {
                    onInterestClick()
                } else {
                    onClick()
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (subject.interestScore == 0) BorderStroke(1.5.dp, Color(0xFFFF9500).copy(alpha = 0.8f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category Badge & Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(categoryColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = subject.kategori.uppercase(),
                        color = categoryColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = subject.kodeMapel,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subject Title
            Text(
                text = subject.nama,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF102B5E),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // JP Subtext
            Text(
                text = "${subject.jamPelajaran} JP per Minggu",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFF2F4F7), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Star Interest Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInterestClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Minat Belajar:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF102B5E)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (subject.interestScore == 0) {
                        Text(
                            text = "Tentukan Minat ⚠️",
                            color = Color(0xFFFF9500),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    } else {
                        repeat(5) { index ->
                            val active = index < subject.interestScore
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (active) Color(0xFFFFCC00) else Color(0xFFD1D1D6),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
