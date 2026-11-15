package com.budgetbook.hello

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(HelloController::class)
class HelloControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {

    @Test
    fun `hello 요청에 인사말, 현재 시각, 자바 버전을 돌려준다`() {
        mockMvc.get("/hello").andExpect {
            status { isOk() }
            jsonPath("$.message") { value("Hello, budget book!") }
            jsonPath("$.currentTime") { isString() }
            jsonPath("$.javaVersion") { value(Runtime.version().toString()) }
        }
    }
}
