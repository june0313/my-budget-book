package com.budgetbook.entry

import com.budgetbook.category.Category
import com.budgetbook.category.CategoryRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** @DataJpaTest는 JPA 관련 빈만 띄우므로 기본 카테고리 등록(ApplicationRunner)이 실행되지 않는다 */
@DataJpaTest
class EntryRepositoryTest(
    @Autowired private val entryRepository: EntryRepository,
    @Autowired private val categoryRepository: CategoryRepository,
) {

    @Test
    fun `거래 내역을 저장하고 다시 조회할 수 있다`() {
        val food = categoryRepository.save(Category(name = "식비", type = EntryType.EXPENSE))
        val saved = entryRepository.save(
            Entry(
                date = LocalDate.of(2026, 11, 15),
                type = EntryType.EXPENSE,
                amount = 12_000,
                category = food,
                memo = "점심",
            )
        )

        val id = assertNotNull(saved.id)
        val found = assertNotNull(entryRepository.findById(id).orElse(null))
        assertEquals(EntryType.EXPENSE, found.type)
        assertEquals(12_000, found.amount)
        assertEquals("식비", found.category.name)
        assertEquals("점심", found.memo)
    }

    @Test
    fun `카테고리가 거래 내역에서 사용 중인지 확인할 수 있다`() {
        val food = categoryRepository.save(Category(name = "식비", type = EntryType.EXPENSE))
        val cafe = categoryRepository.save(Category(name = "카페", type = EntryType.EXPENSE))
        entryRepository.save(Entry(LocalDate.of(2026, 11, 15), EntryType.EXPENSE, 12_000, food))

        assertTrue(entryRepository.existsByCategory(food))
        assertFalse(entryRepository.existsByCategory(cafe))
    }
}
