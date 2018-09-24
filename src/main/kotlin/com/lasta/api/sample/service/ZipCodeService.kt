package com.lasta.api.sample.service

import com.lasta.api.sample.entity.ZipCodeEntity

interface ZipCodeService {
    fun findByZipCode(zipCode: String): Collection<ZipCodeEntity>
}