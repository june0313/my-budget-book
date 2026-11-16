package com.budgetbook.category

import com.budgetbook.entry.Entry
import com.budgetbook.entry.EntryRepository
import com.budgetbook.entry.EntryType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/** 앱 전체를 띄우므로 기본 카테고리가 등록된 상태에서 시작한다 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val categoryRepository: CategoryRepository,
    @Autowired private val entryRepository: EntryRepository,
) {

    private val expenseCount = DefaultCategoryInitializer.EXPENSE_CATEGORIES.size
    private val incomeCount = DefaultCategoryInitializer.INCOME_CATEGORIES.size

    private fun createJson(name: String, type: EntryType) = """{"name": "$name", "type": "$type"}"""

    @Test
    fun `기본 카테고리 전체를 조회한다`() {
        mockMvc.get("/api/categories").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(expenseCount + incomeCount) }
            jsonPath("$[0].isDefault") { value(true) }
        }
    }

    @Test
    fun `구분으로 거를 수 있다`() {
        mockMvc.get("/api/categories?type=INCOME").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(incomeCount) }
            jsonPath("$[0].name") { value("급여") }
            jsonPath("$[0].type") { value("INCOME") }
        }
    }

    @Test
    fun `카테고리를 추가하면 201과 함께 사용자 카테고리로 등록된다`() {
        mockMvc.post("/api/categories") {
            contentType = MediaType.APPLICATION_JSON
            content = createJson("  반려동물  ", EntryType.EXPENSE)
        }.andExpect {
            status { isCreated() }
            header { exists("Location") }
            jsonPath("$.name") { value("반려동물") } // 앞뒤 공백 제거
            jsonPath("$.type") { value("EXPENSE") }
            jsonPath("$.isDefault") { value(false) }
        }
    }

    @Test
    fun `같은 구분에 같은 이름이 있으면 409를 돌려준다`() {
        mockMvc.post("/api/categories") {
            contentType = MediaType.APPLICATION_JSON
            content = createJson("식비", EntryType.EXPENSE)
        }.andExpect {
            status { isConflict() }
            jsonPath("$.detail") { value("이미 있는 카테고리 이름입니다. (EXPENSE, 식비)") }
        }
    }

    @Test
    fun `구분이 다르면 같은 이름도 추가할 수 있다`() {
        mockMvc.post("/api/categories") {
            contentType = MediaType.APPLICATION_JSON
            content = createJson("식비", EntryType.INCOME)
        }.andExpect {
            status { isCreated() }
        }
    }

    @Test
    fun `이름이 비어 있으면 400을 돌려준다`() {
        mockMvc.post("/api/categories") {
            contentType = MediaType.APPLICATION_JSON
            content = createJson(" ", EntryType.EXPENSE)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors[0].field") { value("name") }
        }
    }

    @Test
    fun `카테고리 이름을 바꾼다`() {
        val food = checkNotNull(categoryRepository.findByTypeAndName(EntryType.EXPENSE, "식비"))

        mockMvc.patch("/api/categories/${food.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "외식"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("외식") }
            jsonPath("$.type") { value("EXPENSE") }
        }
    }

    @Test
    fun `같은 구분의 다른 카테고리 이름으로 바꾸면 409를 돌려준다`() {
        val food = checkNotNull(categoryRepository.findByTypeAndName(EntryType.EXPENSE, "식비"))

        mockMvc.patch("/api/categories/${food.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "카페"}"""
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `자기 이름 그대로 바꾸는 것은 허용한다`() {
        val food = checkNotNull(categoryRepository.findByTypeAndName(EntryType.EXPENSE, "식비"))

        mockMvc.patch("/api/categories/${food.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "식비"}"""
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `사용하지 않는 카테고리는 삭제할 수 있다`() {
        val pet = categoryRepository.save(Category(name = "반려동물", type = EntryType.EXPENSE))

        mockMvc.delete("/api/categories/${pet.id}").andExpect { status { isNoContent() } }
        mockMvc.delete("/api/categories/${pet.id}").andExpect { status { isNotFound() } }
    }

    @Test
    fun `거래 내역이 사용 중인 카테고리는 삭제할 수 없다`() {
        val food = checkNotNull(categoryRepository.findByTypeAndName(EntryType.EXPENSE, "식비"))
        entryRepository.save(Entry(LocalDate.of(2026, 11, 15), EntryType.EXPENSE, 12_000, food))

        mockMvc.delete("/api/categories/${food.id}").andExpect {
            status { isConflict() }
            jsonPath("$.detail") { value("거래 내역에서 사용 중인 카테고리는 삭제할 수 없습니다. id=${food.id}") }
        }
    }

    @Test
    fun `없는 카테고리 이름을 바꾸려 하면 404를 돌려준다`() {
        mockMvc.patch("/api/categories/9999") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name": "아무거나"}"""
        }.andExpect {
            status { isNotFound() }
        }
    }
}
