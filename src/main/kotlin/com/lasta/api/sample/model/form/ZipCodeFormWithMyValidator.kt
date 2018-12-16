package com.lasta.api.sample.model.form

import com.lasta.api.sample.model.validator.ValidZipCodeWithMyValidator

@ValidZipCodeWithMyValidator
data class ZipCodeFormWithMyValidator(
//    @field:ValidZipCode(message = "code: zipcode must be seven numeric letters.")
        val code: String?
)
