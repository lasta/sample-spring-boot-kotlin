package com.lasta.api.sample.model.validator

import org.springframework.beans.BeanWrapperImpl
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class ZipCodeFormValidator : ConstraintValidator<ValidZipCodeWithMyValidator, Any> {
    private var message: String = "Zip code must be 7 numeric letters"
    private var codeField: String = ""

    override fun initialize(constraintAnnotarion: ValidZipCodeWithMyValidator?) {
        if (constraintAnnotarion == null) return
        message = constraintAnnotarion.message
        codeField = constraintAnnotarion.codeField
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) return false
        val beanWrapper = BeanWrapperImpl(value)
        val code = beanWrapper.getPropertyValue(codeField)

        if (isValidCode(code)) return true

        applyContext(context!!)
        return false
    }

    private fun isValidCode(code: Any?): Boolean {
        code ?: return false
        if (code !is CharSequence) return false
        if (code.length != 7) return false
        return true
    }

    private fun applyContext(context: ConstraintValidatorContext) {
        context.disableDefaultConstraintViolation()
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(codeField)
                .addConstraintViolation()
    }
}