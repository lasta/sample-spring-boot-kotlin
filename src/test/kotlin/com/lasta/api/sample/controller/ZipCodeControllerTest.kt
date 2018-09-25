package com.lasta.api.sample.controller

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class ZipCodeControllerTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun test_getZipCode_withoutZipCode_thenBadRequest() {
        mvc.perform(MockMvcRequestBuilders.get("/zipcode").accept(MediaType.ALL))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun test_getZipCode_withEmptyZipCode_thenBadRequest() {
        mvc.perform(MockMvcRequestBuilders.get("/zipcode?code=").accept(MediaType.ALL))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun test_getZipCode_with6LetterZipCode_thenBadRequest() {
        mvc.perform(MockMvcRequestBuilders.get("/zipcode?code=123456").accept(MediaType.ALL))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun test_getZipCode_with7LetterZipCode_thenOk() {
        mvc.perform(MockMvcRequestBuilders.get("/zipcode?code=1234567").accept(MediaType.ALL))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun test_getZipCode_with8LetterZipCode_thenBadRequest() {
        mvc.perform(MockMvcRequestBuilders.get("/zipcode?code=12345678").accept(MediaType.ALL))
                .andExpect(status().isBadRequest)
    }
}
