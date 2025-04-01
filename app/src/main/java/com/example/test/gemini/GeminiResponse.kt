package com.example.test.gemini

import com.google.gson.annotations.SerializedName

data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<Candidate>?)

data class Candidate(
    @SerializedName("content") val content: String?
)
