package com.lasta.api.sample.controller

import com.lasta.api.sample.entity.ZipCodeEntity
import com.lasta.api.sample.model.common.CommonParameter
import com.lasta.api.sample.model.form.ZipCodeForm
import com.lasta.api.sample.service.ZipCodeService
import org.hibernate.validator.constraints.Length
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["zipcode"], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
class ZipCodeController(private val service: ZipCodeService, private val commonParameter: CommonParameter) {
    val logger: Logger = LoggerFactory.getLogger(ZipCodeController::class.java)

    @GetMapping
    fun getByZipCode(
            @Validated
            @Length(message = "Zip code must be 7 letters", max = 7, min = 7)
            @RequestParam(name = "code", required = true)
            zipCode: String
//            bindingResult: BindingResult
    ): ResponseEntity<Collection<ZipCodeEntity>> {
//        if (bindingResult.hasErrors()) {
//            return ResponseEntity(HttpStatus.BAD_REQUEST)
//        }
        if (commonParameter.isDebug) {
            logger.debug("DEBUG OPTION IS ENABLED")
        }
        val zipCodeEntities: Collection<ZipCodeEntity> = service.findByZipCode(zipCode)
//                .ifEmpty {
//                    return ResponseEntity(HttpStatus.NOT_FOUND)
//                }
        return ResponseEntity.ok(zipCodeEntities)
    }

    @GetMapping(path = ["/withbean"])
    fun getByZipCode(
            @Validated form: ZipCodeForm,
            bindingResult: BindingResult
    ): ResponseEntity<Collection<ZipCodeEntity>> {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(listOf(ZipCodeEntity()))
        }
        val zipCodeEntities: Collection<ZipCodeEntity> = service.findByZipCode(form.code)
        return ResponseEntity.ok(zipCodeEntities)
    }

    @GetMapping(path = ["/search"])
    fun getByQuery(
            @RequestParam(name = "q", required = true)
            q: String
    ): ResponseEntity<Collection<ZipCodeEntity>> =
            ResponseEntity.ok(service.findByQuery(q))

}
