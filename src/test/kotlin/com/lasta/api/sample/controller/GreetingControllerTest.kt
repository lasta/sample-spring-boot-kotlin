package com.lasta.api.sample.controller

import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Test class for {@link GreetingController}
 */
@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class GreetingControllerTest {

    @Autowired
    lateinit var mvc: MockMvc

    @Test
    fun test_getGreeting_withLegalPhaseParam_thenOk() {
        mvc.perform(MockMvcRequestBuilders.get("/greeting?phase=morning")
                .accept(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isOk)
                .andExpect(content().string(equalTo<String>("Good Morning.")))
    }

    @Test
    fun test_getGreeting_withLegalPhaseParamAndName_thenOk() {
        mvc.perform(MockMvcRequestBuilders.get("/greeting?phase=morning&name=test-name")
                .accept(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isOk)
                .andExpect(content().string(equalTo<String>("Good Morning, test-name.")))
    }

    @Test
    fun test_getGreeting_withIllegalPhaseParam_thenNg() {
        mvc.perform(MockMvcRequestBuilders.get("/greeting?phase=illegal")
                .accept(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isBadRequest)
    }
}