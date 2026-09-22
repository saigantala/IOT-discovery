package com.example.network

import android.content.Context
import android.util.Log
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(private val context: Context) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val tokenManager = TokenManager(context)
        val refreshToken = tokenManager.getRefreshToken()

        if (refreshToken.isNullOrBlank()) {
            Log.d("TokenAuth", "No refresh token available, clearing auth state.")
            tokenManager.clear()
            return null
        }

        // Avoid infinite refresh loops
        if (responseCount(response) >= 2) {
            Log.e("TokenAuth", "Token refresh failed after retry. Logout required.")
            tokenManager.clear()
            return null
        }

        synchronized(this) {
            val currentToken = tokenManager.getAccessToken()
            val requestToken = response.request.header("Authorization")?.replace("Bearer ", "")

            // If token was refreshed by another thread in the meantime, retry with new token
            if (currentToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            Log.d("TokenAuth", "Attempting automatic token refresh via REST API...")
            return try {
                val refreshResponse = RetrofitClient.getApiService(context).refreshSync(mapOf("refreshToken" to refreshToken)).execute()
                if (refreshResponse.isSuccessful && refreshResponse.body()?.success == true) {
                    val authData = refreshResponse.body()?.data
                    if (authData?.accessToken != null && authData.refreshToken != null) {
                        Log.d("TokenAuth", "✅ Token refresh succeeded!")
                        tokenManager.saveTokens(authData.accessToken, authData.refreshToken)
                        return response.request.newBuilder()
                            .header("Authorization", "Bearer ${authData.accessToken}")
                            .build()
                    }
                }
                Log.e("TokenAuth", "❌ Token refresh failed on server. Logging out.")
                tokenManager.clear()
                null
            } catch (e: Exception) {
                Log.e("TokenAuth", "❌ Token refresh exception: ${e.message}", e)
                tokenManager.clear()
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
