package com.lasta.api.sample.service

import com.lasta.api.sample.entity.DetailSolrDocument

interface SearchService {
    fun search(word: String): Collection<DetailSolrDocument>
}
