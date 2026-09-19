package com.example.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content

object GeminiService {
    private val model = Firebase.ai(backend = GenerativeBackend.vertexAI())
        .generativeModel("gemini-1.5-flash")

    suspend fun analyzeDeviceSecurity(deviceInfo: String): String {
        return try {
            val response = model.generateContent(
                content {
                    text("Analyze the following IoT device for security risks: $deviceInfo")
                }
            )
            response.text ?: "No analysis available."
        } catch (e: Exception) {
            "Error analyzing device: ${e.message}"
        }
    }
}
