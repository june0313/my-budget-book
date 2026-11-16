package com.budgetbook.entry

import com.budgetbook.category.Category
import com.budgetbook.category.CategoryRepository
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

/** 앱 전체를 띄우므로 기본 카테고리가 등록된 상태에서 시작한다 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트마다 DB 변경을 롤백
class EntryControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val entryRepository: EntryRepository,
    @Autowired private val categoryRepository: CategoryRepository,
) {

    private fun category(type: EntryType, name: String): Category =
        checkNotNull(categoryRepository.findByTypeAndName(type, name))

    private val food get() = category(EntryType.EXPENSE, "식비")
    private val salary get() = category(EntryType.INCOME, "급여")

    private fun requestJson(
        type: EntryType = EntryType.EXPENSE,
        categoryId: Long? = food.id,
        date: String = "2026-11-15",
        amount: Long = 12000,
    ) = """
        {
          "date": "$date",
          "type": "$type",
          "amount": $amount,
          "categoryId": $categoryId,
          "memo": "점심"
        }
    """.trimIndent()

    private fun saveEntry(date: LocalDate, amount: Long = 10_000) = entryRepository.save(
        Entry(date = date, type = EntryType.EXPENSE, amount = amount, category = food)
    )

    @Test
    fun `거래 내역을 등록하면 201과 Location 헤더, 카테고리 정보를 돌려준다`() {
        mockMvc.post("/api/entries") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson()
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
            jsonPath("$.id") { isNumber() }
            jsonPath("$.amount") { value(12000) }
            jsonPath("$.category.id") { value(food.id) }
            jsonPath("$.category.name") { value("식비") }
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
            jsonPath("$[0].category.name") { value("식비") }
        }
    }

    @Test
    fun `거래 내역을 수정할 수 있다`() {
        val id = saveEntry(LocalDate.of(2026, 11, 1)).id

        mockMvc.put("/api/entries/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson(type = EntryType.INCOME, categoryId = salary.id, amount = 3_000_000)
        }.andExpect {
            status { isOk() }
            jsonPath("$.type") { value("INCOME") }
            jsonPath("$.amount") { value(3_000_000) }
            jsonPath("$.category.name") { value("급여") }
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
    fun `없는 카테고리로 등록하면 400을 돌려준다`() {
        mockMvc.post("/api/entries") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson(categoryId = 9999)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.detail") { value("존재하지 않는 카테고리입니다. categoryId=9999") }
        }
    }

    @Test
    fun `지출 거래에 수입 카테고리를 쓰면 400을 돌려준다`() {
        mockMvc.post("/api/entries") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson(type = EntryType.EXPENSE, categoryId = salary.id)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.detail") { value("거래 구분과 카테고리 구분이 다릅니다. (거래: EXPENSE, 카테고리 '급여': INCOME)") }
        }
    }

    @Test
    fun `카테고리 이름을 바꾸면 기존 거래 내역에도 바뀐 이름이 보인다`() {
        val cafe = categoryRepository.save(Category(name = "카페2", type = EntryType.EXPENSE))
        val id = entryRepository.save(Entry(LocalDate.of(2026, 11, 1), EntryType.EXPENSE, 4_500, cafe)).id
        cafe.name = "커피"
        categoryRepository.saveAndFlush(cafe) // 이름 변경을 DB에 반영

        mockMvc.get("/api/entries/$id").andExpect {
            status { isOk() }
            jsonPath("$.category.name") { value("커피") }
        }
    }
}
