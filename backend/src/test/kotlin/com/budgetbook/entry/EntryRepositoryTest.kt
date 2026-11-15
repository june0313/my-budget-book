package com.budgetbook.entry

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DataJpaTest
class EntryRepositoryTest(
    @Autowired private val entryRepository: EntryRepository,
) {

    @Test
    fun `거래 내역을 저장하고 다시 조회할 수 있다`() {
        val saved = entryRepository.save(
            Entry(
                date = LocalDate.of(2026, 11, 15),
                type = EntryType.EXPENSE,
                amount = 12_000,
                category = "식비",
                memo = "점심",
            )
        )

        val id = assertNotNull(saved.id)
        val found = assertNotNull(entryRepository.findById(id).orElse(null))
        assertEquals(EntryType.EXPENSE, found.type)
        assertEquals(12_000, found.amount)
        assertEquals("식비", found.category)
        assertEquals("점심", found.memo)
    }
}
