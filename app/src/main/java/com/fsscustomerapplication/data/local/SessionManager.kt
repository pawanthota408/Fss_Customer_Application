package com.fsscustomerapplication.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("fss_prefs", Context.MODE_PRIVATE)

    fun saveUserId(userId: Int) {
        prefs.edit().putInt("user_id", userId).apply()
    }

    fun getUserId(): Int {
        return prefs.getInt("user_id", -1)
    }

    fun logout() {
        prefs.edit().remove("user_id").apply()
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != -1
    }

    fun saveUserData(name: String?, email: String?, phone: String?, company: String? = null) {
        prefs.edit()
            .putString("user_name", name ?: "")
            .putString("user_email", email ?: "")
            .putString("user_phone", phone ?: "")
            .putString("user_company", company ?: "")
            .apply()
    }

    fun getUserName(): String = prefs.getString("user_name", "") ?: ""
    fun getUserEmail(): String = prefs.getString("user_email", "") ?: ""
    fun getUserPhone(): String = prefs.getString("user_phone", "") ?: ""
    fun getUserCompany(): String = prefs.getString("user_company", "") ?: ""

    fun saveLanguage(languageCode: String) {
        prefs.edit().putString("app_language", languageCode).apply()
    }

    fun getLanguage(): String {
        return prefs.getString("app_language", "en") ?: "en"
    }
}
