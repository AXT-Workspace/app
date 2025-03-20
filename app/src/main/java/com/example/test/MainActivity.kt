package com.example.test

import android.os.Bundle
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import com.aldebaran.qi.sdk.`object`.conversation.ListenResult
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import com.example.test.ui.theme.TestTheme


class MainActivity : ComponentActivity(), RobotLifecycleCallbacks {
    private var qiContext: QiContext? = null
    private var recognizedText = mutableStateOf("Waiting for input")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = recognizedText.value, modifier = Modifier.padding(16.dp))
                        Button(onClick = { startListening() }) {
                            Text("Listen")
                        }
                    }
                }
            }
        }
        QiSDK.register(this, this)
    }

    private fun startListening() {
        qiContext?.let { context ->
            CoroutineScope(Dispatchers.IO).launch {
                val listen: Listen = ListenBuilder.with(context).build()
                val result: ListenResult = listen.run()
                recognizedText.value = result.heardPhrase.text
            }
        }
    }

    override fun onRobotFocusGained(qiContext: QiContext?) {
        val say: Say = SayBuilder.with(qiContext)
            .withText("Hello there!")
            .build()

        qiContext?.let { context ->
            val humanAwareness: HumanAwareness = context.humanAwareness
            humanAwareness.addOnEngagedHumanChangedListener { human ->
                human?.let {
                    say.run()
                }
            }
        }
    }

    override fun onRobotFocusLost() {
        qiContext = null
    }

    override fun onRobotFocusRefused(reason: String?) {
        TODO("Not yet implemented")
    }
}
