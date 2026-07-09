package com.pab.digitallearning.core

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("DigitalLearningPrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
        const val KEY_TOKEN = "token"
        const val KEY_ROLE = "role"
    }

    fun saveSession(token: String, role: String) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_TOKEN, token)
        editor.putString(KEY_ROLE, role)
        editor.apply()
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getRole(): String? {
        return prefs.getString(KEY_ROLE, null)
    }
}
