package com.lasta.api.sample.service.impl

import com.lasta.api.sample.entity.ZipCodeEntity
import com.lasta.api.sample.model.common.CommonParameter
import com.lasta.api.sample.repository.ZipCodeRepository
import com.lasta.api.sample.service.ZipCodeService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ZipCodeServiceImpl(private val repository: ZipCodeRepository, private val commonParameter: CommonParameter) : ZipCodeService {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ZipCodeServiceImpl::class.java)
    }

    override fun findByZipCode(zipCode: String): Collection<ZipCodeEntity> {
        if (commonParameter.isDebug) {
            LOGGER.debug("DEBUG OPTION IS ENABLES IN ZIP CODE SERVICE")
        }
        return repository.findByZipCode(zipCode)
    }

    override fun findByQuery(query: String): Collection<ZipCodeEntity> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
