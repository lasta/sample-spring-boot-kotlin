package com.lasta.api.sample.service.impl

import com.lasta.api.sample.entity.DetailSolrDocument
import com.lasta.api.sample.service.SearchService
import org.springframework.stereotype.Service

@Service
class SearchServiceImpl: SearchService {
    override fun search(word: String): Collection<DetailSolrDocument> {
        TODO("do something")
    }
}
