package com.budgetbook.local

import com.budgetbook.entry.EntryRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals

// 다른 테스트와 같은 인메모리 DB를 쓰면 샘플 데이터가 섞이므로 이 테스트만 별도 DB를 쓴다
@SpringBootTest(properties = ["spring.datasource.url=jdbc:h2:mem:sample-data-test"])
@ActiveProfiles("local")
class SampleDataLoaderTest(
    @Autowired private val entryRepository: EntryRepository,
) {

    @Test
    fun `local 프로필로 실행하면 샘플 데이터가 들어간다`() {
        assertEquals(12, entryRepository.count())
    }
}
