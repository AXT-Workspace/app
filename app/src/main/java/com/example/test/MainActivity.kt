package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.builder.ListenBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.`object`.conversation.Listen
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldebaran.qi.sdk.QiRobot
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder
import com.aldebaran.qi.sdk.`object`.conversation.ListenResult
import com.aldebaran.qi.sdk.`object`.conversation.PhraseSet
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import com.aldebaran.qi.sdk.`object`.locale.Language
import com.aldebaran.qi.sdk.`object`.locale.Locale
import com.aldebaran.qi.sdk.`object`.locale.Region
import com.example.test.GoogleAPI.SpeechToText
import com.example.test.gemini.GeminiApiService
import com.example.test.gemini.GeminiRequest
import com.example.test.gemini.GeminiResponse
import com.example.test.gemini.RetrofitClient
import com.example.test.humaninteraction.HumanInteractionManager
import com.example.test.ui.theme.TestTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity(), RobotLifecycleCallbacks {
    private var qiContext: QiContext? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var recognizedText = mutableStateOf("Waiting for input")
    private lateinit var speechToText: SpeechToText
    private lateinit var humanInteractionManager: HumanInteractionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        speechToText = SpeechToText(this) { result ->
            recognizedText.value = result
            println("Google STT Result: $result")

            coroutineScope.launch {
                delay(2000)
                geminiResponse(result)
            } }


        setContent { //simple UI with "Pepper Listen" button and Speech-To-Text display
            TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding).fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = recognizedText.value,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(32.dp))

                        Button(onClick = { startListening() },
                            modifier = Modifier.fillMaxWidth(0.8f).height(60.dp)) {
                            Text("Pepper Listen", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        Button(onClick = { speechToText.startListening() },
                            modifier = Modifier.fillMaxWidth(0.8f).height(60.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "SpeechToText",
                                modifier = Modifier.size(30.dp))

                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Speak", fontSize = 18.sp)
                    } } } } }
        QiSDK.register(this, this)
    }

    private fun startListening() {
        coroutineScope.launch {
            while (qiContext == null) run {
                println("QiContext is null, cannot start listening.")
                return@launch
        }

        println("Clicked on the button! I'm listening...") //Log

            qiContext?.let { context ->
                try {
                    val phraseSet: PhraseSet = PhraseSetBuilder.with(qiContext).withTexts("What's up Pepper", "How are you", "Old McDonald had a farm", "rock", "paper").build()

                    val listen: Listen = ListenBuilder.with(qiContext).withPhraseSet(phraseSet).build()

                    val result: ListenResult = withContext(Dispatchers.Default) {
                        listen.run()
                    }

                    val heardPhrase = result.heardPhrase.text
                    println("Is this what you said? $heardPhrase")
                    delay(500)

                        when (heardPhrase) {
                            "What's up Pepper" -> {
                                val say: Say = SayBuilder.with(qiContext)
                                    .withText("Hallo wie gehts")
                                    .build()
                                say.run() }

                            "How are you" -> {
                                val say: Say = SayBuilder.with(qiContext)
                                    .withText("I'm doing lovely, how about you")
                                    .build()
                                say.run() }


                            "Old McDonald had a farm" -> {
                                val say: Say = SayBuilder.with(qiContext)
                                    .withText("You mean old xarop")
                                    .build()
                                say.run() }

                            else -> {
                                val say: Say = SayBuilder.with(qiContext)
                                    .withText("Sorry, I didn't catch that")
                                    .build()
                                say.run() }
                        }


                } catch (e: Exception) {
                    println("Error during listening: ${e.message}")
                } } } }



    private fun geminiResponse(input: String) {
        println("geminiResponse called: $input")

        val shortInput = "Please keep the answer short and in a few sentences. Respond in American English and avoid British spelling. $input"

        coroutineScope.launch {
            try {
                println("Sending request to Gemini API...")

                val response = RetrofitClient.instance.sendMessage(GeminiRequest(shortInput))
                println("Response from Gemini: ${response.response}")

                withContext(Dispatchers.Main) {
                    makePepperSpeak(response.response)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    makePepperSpeak("Error processing your request.")

                }
                println("Error calling Gemini API: ${e.message}")
            }
        }
    }

    private fun makePepperSpeak (text: String) {
            println("makePepperSpeak called with: $text")

            coroutineScope.launch {
                qiContext?.let {
                    println("Pepper is about to drop some bars...")
                    val locale: Locale = Locale(Language.ENGLISH, Region.UNITED_STATES)
                    val say = SayBuilder.with(it).withText(text).withLocale(locale).build()
                    say.run()
                }
            }
        }


        //Pepper says "Hello there!" upon recognising a human
        override fun onRobotFocusGained(qiContext: QiContext?) {
            println("onFocusRobotGained called! qiContext assigned")
            this.qiContext = qiContext

            try {
            val say: Say = SayBuilder.with(qiContext)
                .withText("Hello there!")
                .build()

            qiContext?.let { context ->

                humanInteractionManager = HumanInteractionManager()
                humanInteractionManager.getHumanInfo(context)

                val humanAwareness: HumanAwareness = context.humanAwareness
                humanAwareness.addOnEngagedHumanChangedListener { human ->
                    human?.let {
                        say.run()
                    }
                }
            }
        }
            catch (e: Exception) {
                println("Error onRobotFocusGained: ${e.message}")

            }
        }

        override fun onRobotFocusLost() {
            speechToText.destroy()
            qiContext = null
        }

        override fun onRobotFocusRefused(reason: String?) {
            TODO("Not yet implemented")
        }
    }
