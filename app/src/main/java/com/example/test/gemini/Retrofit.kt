package com.example.test.gemini

import android.telecom.Call
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.HTTP
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GeminiApiService {
    @POST("/chat/simple")
    suspend fun sendMessage(
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://pepper-server-cne0grdsc8eqgygx.germanywestcentral-01.azurewebsites.net/"

    val instance: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}