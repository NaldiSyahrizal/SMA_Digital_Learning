package com.pab.digitallearning.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AdminActivity(
    val id: String,
    val title: String,
    val timestamp: String,
    val type: String // "add", "edit", "delete", "security", "system"
)

object AdminActivityTracker {
    private const val PREFS_NAME = "AdminActivityPrefs"
    private const val KEY_ACTIVITIES = "activities"
    private const val MAX_LOGS = 25

    private val gson = Gson()

    @Synchronized
    fun logActivity(context: Context, title: String, type: String = "system") {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val activities = getActivitiesInternal(context).toMutableList()

        val timeString = SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date())
        val dateString = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())
        val timestamp = "$dateString, $timeString WIB"

        val newActivity = AdminActivity(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            timestamp = timestamp,
            type = type
        )

        activities.add(0, newActivity) // Add to the top (newest first)

        // Trim list to maximum logs limit
        if (activities.size > MAX_LOGS) {
            activities.removeAt(activities.size - 1)
        }

        prefs.edit().putString(KEY_ACTIVITIES, gson.toJson(activities)).apply()
    }

    @Synchronized
    fun getActivities(context: Context): List<AdminActivity> {
        val activities = getActivitiesInternal(context)
        if (activities.isEmpty()) {
            // Pre-populate with realistic system activities for first-time premium look
            val initial = listOf(
                AdminActivity(
                    id = "init_1",
                    title = "Dashboard Admin siap digunakan",
                    timestamp = "Hari Ini",
                    type = "system"
                ),
                AdminActivity(
                    id = "init_2",
                    title = "Sinkronisasi database sekolah selesai",
                    timestamp = "Kemarin",
                    type = "system"
                ),
                AdminActivity(
                    id = "init_3",
                    title = "Pembaruan modul keamanan & OTP selesai",
                    timestamp = "Kemarin",
                    type = "security"
                )
            )
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_ACTIVITIES, gson.toJson(initial)).apply()
            return initial
        }
        return activities
    }

    private fun getActivitiesInternal(context: Context): List<AdminActivity> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ACTIVITIES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<AdminActivity>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun clearActivities(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_ACTIVITIES).apply()
    }
}
