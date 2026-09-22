package com.example.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val tokenManager = TokenManager(context)
        val token = tokenManager.getAccessToken()
        
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        return chain.proceed(requestBuilder.build())
    }
}
