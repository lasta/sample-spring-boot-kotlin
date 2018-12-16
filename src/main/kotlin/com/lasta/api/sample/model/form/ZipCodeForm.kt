package com.lasta.api.sample.model.form

import com.lasta.api.sample.model.validator.ValidZipCode
import javax.validation.constraints.NotNull

data class ZipCodeForm(
    @field:ValidZipCode(message = "zipcode must be seven numeric letters.")
    @field:NotNull
    val code: String
)
