package com.lasta.api.sample.advice.controller

import com.lasta.api.sample.model.common.CommonParameter
import org.hamcrest.Matchers.`is`
import org.junit.Test

import org.junit.Assert.*
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
class DebugControllerAdviceTest {

    @Autowired
    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var commonParameter: CommonParameter

    @Test
    fun bindDebugOption_debugTrue_thenIsDebugTrue() {
        mvc.perform(MockMvcRequestBuilders.get("/hello/world?_debug=on").accept(MediaType.ALL))
                .andExpect(status().isOk)
                .andExpect { assertThat(commonParameter.isDebug, `is`(true)) }
    }

    @Test
    fun bindDebugOption_debugFalse_thenIsDebugFalse() {
        mvc.perform(MockMvcRequestBuilders.get("/hello/world?_debug=on").accept(MediaType.ALL))
                .andExpect(status().isOk)
                .andExpect { assertThat(commonParameter.isDebug, `is`(false)) }
    }

    @Test
    fun bindDebugOption_debugNull_thenIsDebugFalse() {
        mvc.perform(MockMvcRequestBuilders.get("/hello/world").accept(MediaType.ALL))
                .andExpect(status().isOk)
                .andExpect { assertThat(commonParameter.isDebug, `is`(false)) }
    }
}
