package com.budgetbook.common

import com.budgetbook.entry.EntryController
import com.budgetbook.entry.EntryService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

/**
 * 잘못된 요청에 대해 원인을 알 수 있는 400 응답을 주는지 확인한다.
 * 요청이 컨트롤러 메서드에 도달하기 전에 실패하는 경우들이라 서비스는 가짜(Mock)로 둔다.
 */
@WebMvcTest(EntryController::class)
class GlobalExceptionHandlerTest(
    @Autowired private val mockMvc: MockMvc,
) {

    @MockitoBean
    private lateinit var entryService: EntryService

    private fun postEntry(body: String?) = mockMvc.post("/api/entries") {
        contentType = MediaType.APPLICATION_JSON
        if (body != null) content = body
    }

    @Test
    fun `검증 실패 시 필드별 메시지를 모두 돌려준다`() {
        postEntry("""{"date": "2026-11-15", "type": "EXPENSE", "amount": 0, "categoryId": 1, "memo": "${"가".repeat(256)}"}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.detail") { value("입력값이 올바르지 않습니다") }
            jsonPath("$.errors.length()") { value(2) }
            jsonPath("$.errors[?(@.field == 'amount')].message") { value("금액은 0보다 커야 합니다") }
            jsonPath("$.errors[?(@.field == 'memo')].message") { value("메모는 255자 이하로 입력해 주세요") }
        }
    }

    @Test
    fun `본문이 없으면 본문이 비어 있다고 알려준다`() {
        postEntry(null).andExpect {
            status { isBadRequest() }
            jsonPath("$.detail") { value("요청 본문이 비어 있습니다") }
        }
    }

    @Test
    fun `JSON 문법이 틀리면 문법 오류라고 알려준다`() {
        postEntry("""{"date": "2026-11-15", """).andExpect {
            status { isBadRequest() }
            jsonPath("$.detail") { value("JSON 문법이 올바르지 않습니다") }
        }
    }

    @Test
    fun `필수 값이 빠지면 어느 필드인지 알려준다`() {
        postEntry("""{"type": "EXPENSE", "amount": 1000, "categoryId": 1}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("date") }
            jsonPath("$.errors[0].message") { value("필수 값입니다") }
        }
    }

    @Test
    fun `enum에 없는 값이면 허용값을 알려준다`() {
        postEntry("""{"date": "2026-11-15", "type": "FOOD", "amount": 1000, "categoryId": 1}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("type") }
            jsonPath("$.errors[0].message") { value("허용되지 않는 값입니다 (입력값: FOOD, 허용값: INCOME, EXPENSE)") }
        }
    }

    @Test
    fun `날짜 형식이 틀리면 어느 필드인지 알려준다`() {
        postEntry("""{"date": "2026/11/15", "type": "EXPENSE", "amount": 1000, "categoryId": 1}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("date") }
            jsonPath("$.errors[0].message") { value("값의 형식이 올바르지 않습니다 (입력값: 2026/11/15)") }
        }
    }

    @Test
    fun `경로의 id가 숫자가 아니면 알려준다`() {
        mockMvc.get("/api/entries/abc").andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("id") }
            jsonPath("$.errors[0].message") { value("값의 형식이 올바르지 않습니다 (입력값: abc)") }
        }
    }
}
