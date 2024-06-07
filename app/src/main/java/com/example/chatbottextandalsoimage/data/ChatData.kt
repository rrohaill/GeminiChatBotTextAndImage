package com.example.chatbottextandalsoimage.data

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import dev.rrohaill.chatbotexample.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChatData {
    private const val API_KEY = BuildConfig.GEMINI_API_KEY

    suspend fun getResponse(prompt: String): Chat {
        val generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = API_KEY,
            generationConfig = generationConfig {
                temperature = 1f
                topK = 64
                topP = 0.95f
                maxOutputTokens = 8192
                responseMimeType = "text/plain"
            }
        )

        try {
            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(prompt)
            }
            return Chat(
                prompt = response.text ?: "Error",
                bitmap = null,
                isFromUser = false
            )
        } catch (e: Exception) {
            return Chat(
                prompt = e.message ?: "Error",
                bitmap = null,
                isFromUser = false
            )
        }
    }

    suspend fun getResponseWithImage(prompt: String, bitmap: Bitmap): Chat {
        val generativeModel = GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = API_KEY
        )
        val userInput = "Take a look at images, and then answer the following questions: $prompt"

        try {
            val inputContent = content {
                image(bitmap)
                text(userInput)
            }

            val response = withContext(Dispatchers.IO) {
                generativeModel.generateContent(inputContent)
            }
            return Chat(
                prompt = response.text ?: "Error",
                bitmap = null,
                isFromUser = false
            )
        } catch (e: Exception) {
            return Chat(
                prompt = e.message ?: "Error",
                bitmap = null,
                isFromUser = false
            )
        }
    }

}