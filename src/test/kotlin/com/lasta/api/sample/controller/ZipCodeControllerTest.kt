package com.lasta.api.sample.controller

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
class ZipCodeControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Nested
    inner class ZipCodeApi {

        @ParameterizedTest
        @MethodSource("data")
        fun queryThenValidStatus(query: String, expected: ResultMatcher) {
            mvc.perform(MockMvcRequestBuilders.get("/zipcode?$query").accept(MediaType.ALL))
                    .andExpect(expected)
        }
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun data() =
                listOf(
                        arguments("", status().isBadRequest),
                        arguments("code=", status().isBadRequest),
                        arguments("code=123456", status().isBadRequest),
                        arguments("code=1234567", status().isOk),
                        arguments("code=12345678", status().isBadRequest)
                )
    }

//    @Test
//    fun test_getZipCode_withoutZipCode_thenBadRequest() {
//        mvc.perform(MockMvcRequestBuilders.get("/zipcode").accept(MediaType.ALL))
//                .andExpect(status().isBadRequest)
//    }
//
//    @Test
//    fun test_getZipCode_withEmptyZipCode_thenBadRequest() {
//        mvc.perform(MockMvcRequestBuilders.get("/zipcode?code=").accept(MediaType.ALL))
//                .andExpect(status().isBadRequest)
//    }
//
//    @Test
//    fun test_getZipCode_with6LetterZipCode_thenBadRequest() {
//        mvc.perform(MockMvcRequestBuilders.get("/zipcode?code=123456").accept(MediaType.ALL))
//                .andExpect(status().isBadRequest)
//    }
//
//    @Test
//    fun test_getZipCode_with7LetterZipCode_thenOk() {
//        mvc.perform(MockMvcRequestBuilders.get("/zipcode?code=1234567").accept(MediaType.ALL))
//                .andExpect(status().isBadRequest)
//    }
//
//    @Test
//    fun test_getZipCode_with8LetterZipCode_thenBadRequest() {
//        mvc.perform(MockMvcRequestBuilders.get("/zipcode?code=12345678").accept(MediaType.ALL))
//                .andExpect(status().isBadRequest)
//    }

}
