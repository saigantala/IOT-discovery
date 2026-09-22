package com.example.network

import android.content.Context
import com.example.utils.ConfigManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    private var instance: ApiService? = null
    private var currentUrl: String? = null

    private fun createMoshi() = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private fun createOkHttpClient() = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    fun getApiService(context: Context): ApiService {
        val baseUrl = ConfigManager(context).getBaseUrl()
        
        if (instance == null || currentUrl != baseUrl) {
            currentUrl = baseUrl
            instance = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(createOkHttpClient())
                .addConverterFactory(MoshiConverterFactory.create(createMoshi()))
                .build()
                .create(ApiService::class.java)
        }
        return instance!!
    }

    // For compatibility with AppModule if needed, though getApiService(context) is safer
    val apiService: ApiService get() = instance ?: throw IllegalStateException("Call getApiService(context) first")
}
