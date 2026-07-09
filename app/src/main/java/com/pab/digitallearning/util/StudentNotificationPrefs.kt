package com.pab.digitallearning.util

import android.content.Context

class StudentNotificationPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("student_notification_prefs", Context.MODE_PRIVATE)

    fun getReadIds(): Set<String> {
        return prefs.getStringSet("read_ids", emptySet()) ?: emptySet()
    }

    fun markAsRead(id: String) {
        val current = getReadIds().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet("read_ids", current).apply()
    }

    fun markMultipleAsRead(ids: List<String>) {
        val current = getReadIds().toMutableSet()
        current.addAll(ids)
        prefs.edit().putStringSet("read_ids", current).apply()
    }

    fun getDeletedIds(): Set<String> {
        return prefs.getStringSet("deleted_ids", emptySet()) ?: emptySet()
    }

    fun deleteNotification(id: String) {
        val current = getDeletedIds().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet("deleted_ids", current).apply()
    }

    fun deleteMultipleNotifications(ids: List<String>) {
        val current = getDeletedIds().toMutableSet()
        current.addAll(ids)
        prefs.edit().putStringSet("deleted_ids", current).apply()
    }
}
