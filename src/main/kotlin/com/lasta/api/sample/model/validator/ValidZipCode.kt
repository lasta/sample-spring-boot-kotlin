package com.lasta.api.sample.model.validator

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [])
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.VALUE_PARAMETER)
@Retention
@ReportAsSingleViolation
@Pattern(regexp = "[0-9]{7}", message = "Zip code must be 7 numeric letters.")
annotation class ValidZipCode(
        // default message
        val message: String = "{com.lasta.api.sample.model.validator.message}",
        // {@code groups} must be member in validation annotation class
        val groups: Array<KClass<*>> = [],
        // {@code payload} must be member in validation annotation class
        val payload: Array<KClass<out Payload>> = []
)
