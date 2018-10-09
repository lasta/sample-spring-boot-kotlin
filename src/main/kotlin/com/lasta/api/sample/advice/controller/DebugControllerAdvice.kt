package com.lasta.api.sample.advice.controller

import com.lasta.api.sample.model.common.CommonParameter
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestParam

@ControllerAdvice
class DebugControllerAdvice(private val commonParameter: CommonParameter) {
    @ModelAttribute
    fun bindDebugOption(
            @RequestParam(name = "_debug") isDebug: Boolean?
    ) {
        commonParameter.isDebug = isDebug ?: false
    }
}
