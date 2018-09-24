package com.lasta.api.sample.repository

import com.lasta.api.sample.entity.ZipCodeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ZipCodeRepository: JpaRepository<ZipCodeEntity, Long>