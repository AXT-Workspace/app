package com.example.test.humaninteraction

import android.util.Log
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiRobot
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.`object`.human.SmileState
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HumanInteractionManager {

    fun getHumanInfo(context: QiContext) {
        val humanAwareness = context.humanAwareness

        humanAwareness.addOnHumansAroundChangedListener { humans ->
            if (humans.isNotEmpty()) {
                val firstHuman = humans[0]
                val estimatedAge = firstHuman.estimatedAge?.years ?: "unknown"
                val estimatedGender = firstHuman.estimatedGender ?: "unknown"
                val smileState = firstHuman.facialExpressions.smile

                Log.i("HumanInfo", "Age: $estimatedAge, Gender: $estimatedGender, Smiling: $smileState")

                val emotion = firstHuman.emotion.pleasure ?: "neutral"
                Log.i("HumanInfo", "Emotion: $emotion")

                CoroutineScope(Dispatchers.Main).launch {
                    if (smileState == SmileState.SMILING) {
                        val response = ("I see that you're happy. Yippie.")
                        val say = SayBuilder.with(context).withText(response).build()
                        say.run()
                    }
                }
            }
        }
    }
}

