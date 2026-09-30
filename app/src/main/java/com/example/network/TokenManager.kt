package com.example.network

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = getPrefs(context.applicationContext)

    fun saveTokens(access: String, refresh: String) {
        prefs.edit().putString("access_token", access).putString("refresh_token", refresh).apply()
    }
    fun getAccessToken(): String? = prefs.getString("access_token", null)
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)
    fun clear() = prefs.edit().clear().apply()

    companion object {
        @Volatile private var cached: SharedPreferences? = null

        private fun getPrefs(ctx: Context): SharedPreferences =
            cached ?: synchronized(this) {
                cached ?: create(ctx).also { cached = it }
            }

        private fun create(ctx: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            return EncryptedSharedPreferences.create(
                ctx, "auth_prefs", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}
