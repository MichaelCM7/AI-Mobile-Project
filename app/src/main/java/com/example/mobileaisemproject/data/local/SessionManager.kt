package com.example.mobileaisemproject.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("app_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        const val NO_USER_ID = -1L
    }

    var currentUserId: Long
        get() = prefs.getLong(KEY_CURRENT_USER_ID, NO_USER_ID)
        set(value) {
            prefs.edit().putLong(KEY_CURRENT_USER_ID, value).apply()
        }

    fun clearSession() {
        prefs.edit().remove(KEY_CURRENT_USER_ID).apply()
    }

    fun isLoggedIn(): Boolean = currentUserId != NO_USER_ID
}
