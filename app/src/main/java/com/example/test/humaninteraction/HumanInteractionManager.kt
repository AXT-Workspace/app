package com.example.test.humaninteraction

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiRobot
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness


class HumanInteractionManager (public val robot: QiRobot) {

    fun makeRockGesture() {
        println("Making Rock gesture")
    }

    fun makeScissorsGesture() {
        println("Making Scissors gesture")
    }

    fun makePaperGesture() {
        println("Making Paper gesture")
    }
}
