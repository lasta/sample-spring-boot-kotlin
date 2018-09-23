package com.lasta.api.sample.service

import com.lasta.api.sample.constant.GreetingPhase

interface GreetingService {
    fun greet(phase: GreetingPhase, name: String?): String
}