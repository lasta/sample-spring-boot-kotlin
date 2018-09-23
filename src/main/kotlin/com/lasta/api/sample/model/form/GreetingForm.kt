package com.lasta.api.sample.model.form

import com.lasta.api.sample.constant.GreetingPhase
import java.io.Serializable
import javax.validation.constraints.NotNull

class GreetingForm : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }

    @NotNull
    lateinit var phase: GreetingPhase

    var name: String? = null
}