package com.lasta.api.sample.model.validator

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ZipCodeFormValidator::class])
annotation class ValidZipCodeWithMyValidator(
        // default message
        val message: String = "",
        // {@code groups} must be member in validation annotation class
        @Suppress("unused") val groups: Array<KClass<*>> = [],
        // {@code payload} must be member in validation annotation class
        @Suppress("unused") val payload: Array<KClass<out Payload>> = [],

        // validation field name
        val codeField: String = "code"
)
