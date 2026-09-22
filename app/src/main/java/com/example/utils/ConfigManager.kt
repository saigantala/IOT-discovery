package com.example.utils

import android.content.Context
import android.content.SharedPreferences

class ConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BACKEND_IP = "backend_ip"
        // Current PC Wi-Fi IP address
        const val DEFAULT_IP = "172.30.116.78"
    }

    fun getBackendIp(): String {
        return prefs.getString(KEY_BACKEND_IP, DEFAULT_IP) ?: DEFAULT_IP
    }

    /**
     * Validates whether a given string is a valid IPv4 address, localhost, or emulator loopback IP.
     */
    fun isValidIpAddress(ip: String): Boolean {
        val cleanIp = ip.trim()
        if (cleanIp.equals("localhost", ignoreCase = true) || cleanIp == "10.0.2.2") {
            return true
        }
        val ipv4Regex = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$".toRegex()
        return cleanIp.matches(ipv4Regex)
    }

    /**
     * Saves the backend IP address if it passes IPv4 validation.
     * @return true if saved successfully, false if invalid IP.
     */
    fun setBackendIp(ip: String): Boolean {
        val cleanIp = ip.trim()
        if (isValidIpAddress(cleanIp)) {
            prefs.edit().putString(KEY_BACKEND_IP, cleanIp).apply()
            return true
        }
        return false
    }

    /**
     * Constructs the full base API URL for Retrofit client.
     */
    fun getBaseUrl(): String {
        val ip = getBackendIp()
        return "http://$ip:4000/api/v1/"
    }
}
