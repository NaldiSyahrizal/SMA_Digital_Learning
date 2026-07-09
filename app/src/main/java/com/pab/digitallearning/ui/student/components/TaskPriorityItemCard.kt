package com.pab.digitallearning.ui.student.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
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
import com.pab.digitallearning.data.model.StudentPriorityTask
import com.pab.digitallearning.util.DateTimeUtils

@Composable
fun TaskPriorityItemCard(
    task: StudentPriorityTask,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val countdown = DateTimeUtils.getCountdownString(task.dueDate)
    val isOverdue = countdown.contains("Terlewati", ignoreCase = true)
    
    val (typeLabel, typeColor, typeIcon) = if (task.tipe == "kuis") {
        Triple("KUIS UJIAN", Color(0xFFFFCC00), Icons.Default.Edit)
    } else {
        Triple("TUGAS HARIAN", Color(0xFF007AFF), Icons.Default.List)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = typeLabel,
                                color = typeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF0F4F8), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = task.subjectName,
                            color = Color(0xFF102B5E),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                BadgePriority(sawScore = task.sawScore)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body Row (Title + Description + Circular SAW score)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.judul,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF102B5E),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.deskripsi,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // SAW Score display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFEBF3FC), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${task.sawScore.toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF102B5E)
                        )
                    }
                    Text(
                        text = "Prioritas",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF2F4F7), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Footer Row (Due Date + Attempt Count)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = if (isOverdue) Color(0xFFFF3B30) else Color(0xFF007AFF),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = countdown,
                        color = if (isOverdue) Color(0xFFFF3B30) else Color(0xFF102B5E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val attemptString = if (task.tipe == "kuis") {
                    val max = task.quizMaxAttempts ?: 0
                    if (max > 0) "Percobaan: ${task.attemptsCount}/$max" else "Percobaan: ${task.attemptsCount} (Bebas)"
                } else {
                    if (task.attemptsCount > 0) "Sudah Dikumpul" else "Belum Dikumpul"
                }

                Text(
                    text = attemptString,
                    color = if (task.attemptsCount > 0) Color(0xFF34C759) else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
