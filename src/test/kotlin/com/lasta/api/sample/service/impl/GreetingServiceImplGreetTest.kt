package com.lasta.api.sample.service.impl

import com.lasta.api.sample.constant.GreetingPhase
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class GreetingServiceImplGreetTest(val phase: GreetingPhase, val name: String?, val expected: String) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<out Any?>> {
            return listOf(
                    arrayOf(GreetingPhase.MORNING, "name", "${GreetingPhase.MORNING.greeting}, name."),
                    arrayOf(GreetingPhase.NOON, "name", "${GreetingPhase.NOON.greeting}, name."),
                    arrayOf(GreetingPhase.EVENING, "name", "${GreetingPhase.EVENING.greeting}, name."),
                    arrayOf(GreetingPhase.MORNING, "", "${GreetingPhase.MORNING.greeting}."),
                    arrayOf(GreetingPhase.NOON, "", "${GreetingPhase.NOON.greeting}."),
                    arrayOf(GreetingPhase.EVENING, "", "${GreetingPhase.EVENING.greeting}."),
                    arrayOf(GreetingPhase.MORNING, null, "${GreetingPhase.MORNING.greeting}."),
                    arrayOf(GreetingPhase.NOON, null, "${GreetingPhase.NOON.greeting}."),
                    arrayOf(GreetingPhase.EVENING, null, "${GreetingPhase.EVENING.greeting}.")
            )
        }
    }

    @Test
    fun test_greet() {
        assertThat(GreetingServiceImpl().greet(phase, name), `is`(expected))
    }

}