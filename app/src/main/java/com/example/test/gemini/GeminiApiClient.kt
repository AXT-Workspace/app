package com.example.test.gemini

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GeminiApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GeminiApiService = retrofit.create(GeminiApiService::class.java)
}