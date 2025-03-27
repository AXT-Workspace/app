package com.example.test

import android.os.Bundle
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.aldebaran.qi.sdk.QiRobot
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder
import com.aldebaran.qi.sdk.`object`.conversation.ListenResult
import com.aldebaran.qi.sdk.`object`.conversation.Phrase
import com.aldebaran.qi.sdk.`object`.conversation.PhraseSet
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import com.example.test.GoogleAPI.SpeechToText
import com.example.test.humaninteraction.HumanInteractionManager
import com.example.test.ui.theme.TestTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity(), RobotLifecycleCallbacks {
    private var qiContext: QiContext? = null
    private var recognizedText = mutableStateOf("Waiting for input")
    private lateinit var speechToText: SpeechToText
    private lateinit var humanInteractionManager: HumanInteractionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        speechToText = SpeechToText(this) { result ->
            recognizedText.value = result
            println("Google STT Result: $result")
            triggerGestureBasedOnSpeech(result) }


        setContent { //simple UI with "Listen" button and Speech-To-Text display
            TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = recognizedText.value, modifier = Modifier.padding(16.dp))
                        Button(onClick = { startListening() }) {
                            Text("Listen")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(onClick = { speechToText.startListening() }) {
                            Icon(Icons.Default.Star, contentDescription = "SpeechToText")
                    } } } } }
        QiSDK.register(this, this)
    }

    private fun startListening() {
        CoroutineScope(Dispatchers.IO).launch {
            while (qiContext == null) {
                println("QiContext is null, cannot start listening.")
                delay(100)
        }

        println("Clicked on the button! I'm listening...") //Log

            qiContext?.let { context ->
                try {
                    val phraseSet: PhraseSet = PhraseSetBuilder.with(qiContext).withTexts("What's up Pepper", "How are you", "Old McDonald had a farm", "rock", "paper",).build()

                    val listen: Listen = ListenBuilder.with(qiContext).withPhraseSet(phraseSet).build()

                    val result: ListenResult = withContext(Dispatchers.Default) {
                        listen.run()
                    }

                    val heardPhrase = result.heardPhrase.text
                    println("Is this what you said? $heardPhrase")
                    delay(500)

                    withContext(Dispatchers.Default) {
                        recognizedText.value = result.heardPhrase.text
                        println("line 86!\n")

                        when (heardPhrase) {
                            "What's up Pepper" -> {
                                val say: Say = SayBuilder.with(qiContext)
                                    .withText("What's cooking, good looking")
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
                        } }


                } catch (e: Exception) {
                    println("Error during listening: ${e.message}")
                } } } }



    private fun triggerGestureBasedOnSpeech(heardPhrase: String) {
        when (heardPhrase.lowercase()) {
            "rock" -> { humanInteractionManager.makeRockGesture()
                println("line 146")  }
            "scissors" -> humanInteractionManager.makeScissorsGesture()
            "paper" -> humanInteractionManager.makePaperGesture()
            else -> println("Unrecognized speech")
        }
    }


    //Pepper says "Hello there!" upon recognising a human
    override fun onRobotFocusGained(qiContext: QiContext?) {
        this.qiContext = qiContext

        val say: Say = SayBuilder.with(qiContext)
            .withText("Five little monkeys jumping on the bed")
            .build()

        qiContext?.let { context ->

            val robot: QiRobot = humanInteractionManager.robot
            if (robot == null) return
            humanInteractionManager = HumanInteractionManager(robot)

            val humanAwareness: HumanAwareness = context.humanAwareness
            humanAwareness.addOnEngagedHumanChangedListener { human ->
                human?.let {
                    say.run()
                } } } }


    override fun onRobotFocusLost() {
        TODO("Not yet implemented")
    }

    override fun onRobotFocusRefused(reason: String?) {
        TODO("Not yet implemented")
    }
}
