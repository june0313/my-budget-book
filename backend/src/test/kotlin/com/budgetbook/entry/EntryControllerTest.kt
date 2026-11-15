package com.budgetbook.entry

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트마다 DB 변경을 롤백
class EntryControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val entryRepository: EntryRepository,
) {

    private val lunchJson = """
        {
          "date": "2026-11-15",
          "type": "EXPENSE",
          "amount": 12000,
          "category": "식비",
          "memo": "점심"
        }
    """.trimIndent()

    private fun saveEntry(date: LocalDate, amount: Long = 10_000) = entryRepository.save(
        Entry(date = date, type = EntryType.EXPENSE, amount = amount, category = "식비")
    )

    @Test
    fun `거래 내역을 등록하면 201과 Location 헤더를 돌려준다`() {
        mockMvc.post("/api/entries") {
            contentType = MediaType.APPLICATION_JSON
            content = lunchJson
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
            jsonPath("$.id") { isNumber() }
            jsonPath("$.category") { value("식비") }
            jsonPath("$.amount") { value(12000) }
        }
    }

    @Test
    fun `목록은 최신 날짜부터 정렬된다`() {
        saveEntry(LocalDate.of(2026, 11, 1))
        saveEntry(LocalDate.of(2026, 11, 15))

        mockMvc.get("/api/entries").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].date") { value("2026-11-15") }
            jsonPath("$[1].date") { value("2026-11-01") }
        }
    }

    @Test
    fun `거래 내역을 수정할 수 있다`() {
        val id = saveEntry(LocalDate.of(2026, 11, 1)).id

        mockMvc.put("/api/entries/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = lunchJson
        }.andExpect {
            status { isOk() }
            jsonPath("$.date") { value("2026-11-15") }
            jsonPath("$.amount") { value(12000) }
            jsonPath("$.memo") { value("점심") }
        }
    }

    @Test
    fun `거래 내역을 삭제하면 이후 조회 시 404가 된다`() {
        val id = saveEntry(LocalDate.of(2026, 11, 1)).id

        mockMvc.delete("/api/entries/$id").andExpect { status { isNoContent() } }
        mockMvc.get("/api/entries/$id").andExpect { status { isNotFound() } }
    }

    @Test
    fun `없는 id를 조회하면 404와 에러 메시지를 돌려준다`() {
        mockMvc.get("/api/entries/9999").andExpect {
            status { isNotFound() }
            jsonPath("$.detail") { value("거래 내역을 찾을 수 없습니다. id=9999") }
        }
    }

    @Test
    fun `금액이 0 이하이거나 카테고리가 비어 있으면 400을 돌려준다`() {
        mockMvc.post("/api/entries") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"date": "2026-11-15", "type": "EXPENSE", "amount": 0, "category": " "}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `필수 값이 빠지면 400을 돌려준다`() {
        mockMvc.post("/api/entries") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"type": "EXPENSE", "category": "식비"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
