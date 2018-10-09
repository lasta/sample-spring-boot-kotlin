package com.lasta.api.sample.model.common

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommonParameterBinder {
    @Bean
    fun commonParameter(): CommonParameter {
        return CommonParameter()
    }
}
