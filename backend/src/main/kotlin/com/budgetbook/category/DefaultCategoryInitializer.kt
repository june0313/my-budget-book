package com.budgetbook.category

import com.budgetbook.entry.EntryType
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 기본 카테고리(C-2)를 등록한다.
 *
 * 샘플 데이터와 달리 앱의 기능이므로 프로필과 관계없이 항상 동작한다.
 * 기본 카테고리가 이미 있으면 아무것도 하지 않는다.
 *
 * `@Order(1)`: 거래 내역 샘플 데이터(SampleDataLoader)가 카테고리를 쓰므로 그보다 먼저 실행한다.
 */
@Component
@Order(1)
class DefaultCategoryInitializer(
    private val categoryRepository: CategoryRepository,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments) {
        if (categoryRepository.existsByIsDefaultTrue()) return

        val defaults =
            EXPENSE_CATEGORIES.map { Category(name = it, type = EntryType.EXPENSE, isDefault = true) } +
                INCOME_CATEGORIES.map { Category(name = it, type = EntryType.INCOME, isDefault = true) }

        categoryRepository.saveAll(defaults)
        log.info("기본 카테고리 {}개를 등록했습니다.", defaults.size)
    }

    companion object {
        val EXPENSE_CATEGORIES = listOf("식비", "카페", "교통", "주거", "통신", "쇼핑", "문화", "의료", "기타")
        val INCOME_CATEGORIES = listOf("급여", "부수입", "용돈", "기타")
    }
}
