package com.lasta.api.sample.model.form

import com.lasta.api.sample.model.validator.ValidZipCode
import javax.validation.constraints.NotNull

data class ZipCodeForm(
        @field:NotNull
        @field:ValidZipCode(message = "code: zipcode must be seven numeric letters.")
        val code: String?
)
