package com.lasta.api.sample.controller

import com.lasta.api.sample.entity.ZipCodeEntity
import com.lasta.api.sample.service.ZipCodeService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.Size

@RestController
@RequestMapping(path = ["zipcode"])
class ZipCodeController(private val service: ZipCodeService) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun getByZipCode(
            @Size(message = "code must be 7 letters.", max = 7, min = 7)
            @RequestParam(name = "code", required = true)
            zipCode: String
    ): ResponseEntity<Collection<ZipCodeEntity>> {
        val zipCodeEntities: Collection<ZipCodeEntity>? = service.findByZipCode(zipCode)

        return zipCodeEntities
                ?.let { ResponseEntity.ok(zipCodeEntities) }
                ?: run { ResponseEntity<Collection<ZipCodeEntity>>(HttpStatus.NOT_FOUND) }
    }
}