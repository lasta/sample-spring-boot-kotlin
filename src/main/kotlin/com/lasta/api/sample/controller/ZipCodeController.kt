package com.lasta.api.sample.controller

import com.lasta.api.sample.entity.ZipCodeEntity
import com.lasta.api.sample.service.ZipCodeService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.Size

@RestController
@Validated
@RequestMapping(path = ["zipcode"])
class ZipCodeController(private val service: ZipCodeService) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun getByZipCode(
            @Valid
            @Size(message = "code must be 7 letters.", max = 7, min = 7)
            @RequestParam(name = "code", required = true)
            zipCode: String
    ): ResponseEntity<Collection<ZipCodeEntity>> {
        val zipCodeEntities: Collection<ZipCodeEntity> = service.findByZipCode(zipCode)

        if (zipCodeEntities.isEmpty()) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        return ResponseEntity.ok(zipCodeEntities)
    }
}
