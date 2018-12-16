package com.lasta.api.sample.model.validator

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import javax.validation.ConstraintValidatorContext

// XXX: not works
internal class ZipCodeFormValidatorTest {

    @Mock
    lateinit var context: ConstraintValidatorContext

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @ParameterizedTest
    @MethodSource("cases")
    fun validationTest(code: String?, expected: Boolean) {
        val actual: Boolean = ZipCodeFormValidator().isValid(code, context)
        assertThat(actual, equalTo(expected))
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun cases(): List<Arguments> = listOf(
                arguments(null, false),
                arguments("", false),
                arguments("123456", false),
                arguments("1234567", true),
                arguments("12345678", false),
                arguments("abcdefg", false)
        )
    }
}