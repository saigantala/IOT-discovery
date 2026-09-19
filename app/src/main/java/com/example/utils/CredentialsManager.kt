package com.example.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Securely maintains live credentials and session tokens.
 */
class CredentialsManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiToken(token: String) {
        sharedPreferences.edit().putString("api_token", token).apply()
    }

    fun getApiToken(): String? {
        return sharedPreferences.getString("api_token", null)
    }

    fun saveLiveCredential(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getLiveCredential(key: String): String? {
        return sharedPreferences.getString(key, null)
    }
    
    fun clearCredentials() {
        sharedPreferences.edit().clear().apply()
    }
}
