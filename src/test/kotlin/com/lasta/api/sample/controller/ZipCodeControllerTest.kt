package com.lasta.api.sample.controller

import com.lasta.api.sample.service.ZipCodeService
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.stream.Stream


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ZipCodeControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    lateinit var service: ZipCodeService

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(service.findByQuery(anyString())).thenReturn(emptyList())
    }

    @Nested
    inner class `Zip code api with javax annotation validation` {

        @ParameterizedTest
        @ArgumentsSource(TestCaseProvider::class)
        fun `When given code parameter then returns OK or BadRequest`(query: String, matcher: ResultMatcher) {
            mvc.perform(MockMvcRequestBuilders.get("/zipcode?$query").accept(MediaType.ALL))
                    .andExpect(matcher)
        }
    }

    @Nested
    inner class `Zip code api with annotation combination validation` {
        @ParameterizedTest
        @ArgumentsSource(TestCaseProvider::class)
        fun `When given code parameter then returns OK or BadRequest`(query: String, matcher: ResultMatcher) {
            mvc.perform(MockMvcRequestBuilders.get("/zipcode/fieldvalidator?$query").accept(MediaType.ALL))
                    .andExpect(matcher)
        }
    }

    @Nested
    inner class `Zip code api with original validation` {
        @ParameterizedTest
        @ArgumentsSource(TestCaseProvider::class)
        fun `When given code parameter then returns OK or BadRequest`(query: String, matcher: ResultMatcher) {
            mvc.perform(MockMvcRequestBuilders.get("/zipcode/classvalidator?$query").accept(MediaType.ALL))
                    .andExpect(matcher)
        }
    }

    internal class TestCaseProvider : ArgumentsProvider {
        // {@code status()} を読んでおり static にはならないので companion object でパラメータを定義できない
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> = Stream.of(
                arguments("", status().isBadRequest),
                arguments("code=", status().isBadRequest),
                arguments("code=123456", status().isBadRequest),
                arguments("code=1234567", status().isOk),
                arguments("code=12345678", status().isBadRequest)
        )
    }

}
