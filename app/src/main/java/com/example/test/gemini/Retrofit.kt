package com.example.test.gemini

import android.telecom.Call
import retrofit2.Response
import retrofit2.http.HTTP
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GeminiApiService {
    @POST("/chat/simple")
    suspend fun getResponse(
        @Header("Authorization")  apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}