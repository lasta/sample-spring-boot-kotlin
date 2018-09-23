package com.lasta.api.sample.controller

import com.lasta.api.sample.constant.GreetingPhase
import com.lasta.api.sample.model.converter.GreetingPhaseConverter
import com.lasta.api.sample.model.form.GreetingForm
import com.lasta.api.sample.service.GreetingService
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// with {@code @RequestParam}
// import javax.validation.constraints.NotNull

// with {@code @RequestParam}
// @Validated
@RestController
@RequestMapping(path = ["greeting"])
class GreetingController(private val service: GreetingService) {

//    with {@code @RequestParam}
//    @GetMapping(produces = [MediaType.TEXT_PLAIN_VALUE])
//    fun greet(@NotNull @RequestParam(value = "phase") phase: GreetingPhase,
//              @RequestParam(value = "name") name: String?): String {
//        return service.greet(phase, name)
//    }

    @GetMapping(produces = [MediaType.TEXT_PLAIN_VALUE])
    fun greet(@ModelAttribute @Validated form: GreetingForm): String {
        return service.greet(form.phase, form.name)
    }

    @InitBinder
    fun initBinder(webDataBinder: WebDataBinder) {
        webDataBinder.registerCustomEditor(GreetingPhase::class.java, GreetingPhaseConverter())
    }
}

