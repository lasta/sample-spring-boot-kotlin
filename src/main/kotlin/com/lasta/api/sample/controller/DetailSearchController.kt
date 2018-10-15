package com.lasta.api.sample.controller

import com.lasta.api.sample.entity.DetailSolrDocument
import com.lasta.api.sample.service.SearchService
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping(path = ["search"], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
class DetailSearchController(private val service: SearchService) {

    @GetMapping
    fun find(@RequestParam(name = "q", required = true) q: String): Collection<DetailSolrDocument> {
        return service.search(q)
    }

}
