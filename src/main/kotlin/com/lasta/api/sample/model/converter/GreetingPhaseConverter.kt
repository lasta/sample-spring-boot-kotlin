package com.lasta.api.sample.model.converter

import com.lasta.api.sample.constant.GreetingPhase
import java.beans.PropertyEditorSupport

class GreetingPhaseConverter : PropertyEditorSupport() {
    @Throws(IllegalArgumentException::class)
    override fun setAsText(text: String?) {
        value = GreetingPhase.fromValue(text)
    }
}