package com.lasta.api.sample.service.impl

import com.lasta.api.sample.entity.ZipCodeEntity
import com.lasta.api.sample.repository.ZipCodeRepository
import com.lasta.api.sample.service.ZipCodeService
import org.springframework.stereotype.Service

@Service
class ZipCodeServiceImpl(private val repository: ZipCodeRepository) : ZipCodeService {
    override fun findByZipCode(zipCode: String): Collection<ZipCodeEntity> = repository.findByZipCode(zipCode)
}