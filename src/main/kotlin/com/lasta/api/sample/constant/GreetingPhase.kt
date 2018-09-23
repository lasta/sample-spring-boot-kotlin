package com.lasta.api.sample.constant

enum class GreetingPhase(val greeting: String) {
    MORNING(greeting = "Good Morning"),
    NOON(greeting = "Good afternoon"),
    EVENING(greeting = "Good evening");

    companion object {
        @Throws(IllegalArgumentException::class)
        fun fromValue(text: String?): GreetingPhase {
            text ?: throw IllegalArgumentException()

            return GreetingPhase.valueOf(text.toUpperCase())
        }
    }
}