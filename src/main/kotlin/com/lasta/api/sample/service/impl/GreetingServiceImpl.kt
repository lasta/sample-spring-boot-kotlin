package com.lasta.api.sample.service.impl

import com.lasta.api.sample.constant.GreetingPhase
import com.lasta.api.sample.service.GreetingService
import org.springframework.stereotype.Service

@Service
class GreetingServiceImpl : GreetingService {
    override fun greet(phase: GreetingPhase, name: String?): String {
        if (name.isNullOrBlank()) {
            return "${phase.greeting}."
        }
        return "${phase.greeting}, $name."
    }
}