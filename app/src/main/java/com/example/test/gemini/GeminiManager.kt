package com.example.test.gemini

//import kotlinx.coroutines.launch
//import com.example.test.gemini.GeminiResponse
//import com.example.test.gemini.GeminiRequest
//import com.example.test.gemini.GeminiApiClient
//import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.builder.SayBuilder
import kotlinx.coroutines.withContext

class GeminiManager(private val qiContext: QiContext?) {

    private val GeminiApiClient = GeminiApiClient()

    suspend fun getGeminiResponse(input: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = GeminiApiClient.api.getResponse(GEMINI_API_KEY, GeminiRequest(prompt = input))

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val geminiText = responseBody?.candidates?.firstOrNull()?.content ?: "I didn't understand that."

                    geminiText

                } else {
                    "Error: ${response.errorBody()?.string()?: "Unknown error"}"
                }

        } catch (e: Exception) {
            println("Error with Gemini response: ${e.message}")
            return@withContext "I encountered an error while processing your request."
        }
    } }

    // Function to make Pepper say the response
    suspend fun makePepperSpeak(input: String) {
        val responseText = getGeminiResponse(input)
        println("Here's your reply: $responseText")

        withContext(Dispatchers.Main) {
            qiContext?.let {
                val say = SayBuilder.with(it).withText(responseText).build()
                say.run()
            }
        }
    }

}