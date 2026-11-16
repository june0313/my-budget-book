package com.budgetbook.local

import com.budgetbook.category.CategoryRepository
import com.budgetbook.entry.Entry
import com.budgetbook.entry.EntryRepository
import com.budgetbook.entry.EntryType
import com.budgetbook.entry.EntryType.EXPENSE
import com.budgetbook.entry.EntryType.INCOME
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * 로컬 개발용 샘플 데이터를 넣는다.
 *
 * - `local` 프로필에서만 동작한다. (application.properties에서 기본 프로필을 local로 지정)
 * - 날짜는 실행한 날 기준 이번 달/지난달로 만들어서, 언제 실행해도 "최근 데이터"가 있다.
 * - 이미 데이터가 있으면 넣지 않는다. (나중에 데이터가 유지되는 DB로 바꿔도 중복되지 않도록)
 * - 기본 카테고리를 쓰므로 DefaultCategoryInitializer(`@Order(1)`) 다음에 실행한다.
 */
@Component
@Profile("local")
@Order(2)
class SampleDataLoader(
    private val entryRepository: EntryRepository,
    private val categoryRepository: CategoryRepository,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments) {
        if (entryRepository.count() > 0) {
            log.info("이미 데이터가 있어 샘플 데이터를 넣지 않습니다.")
            return
        }

        val thisMonth = LocalDate.now().withDayOfMonth(1)
        val lastMonth = thisMonth.minusMonths(1)

        fun entry(date: LocalDate, type: EntryType, amount: Long, categoryName: String, memo: String?) = Entry(
            date = date,
            type = type,
            amount = amount,
            category = checkNotNull(categoryRepository.findByTypeAndName(type, categoryName)) {
                "기본 카테고리에 '$categoryName'($type)이 없습니다"
            },
            memo = memo,
        )

        val samples = listOf(
            // 지난달
            entry(lastMonth.withDayOfMonth(1), INCOME, 3_000_000, "급여", "월급"),
            entry(lastMonth.withDayOfMonth(3), EXPENSE, 550_000, "주거", "월세"),
            entry(lastMonth.withDayOfMonth(7), EXPENSE, 32_000, "식비", "장보기"),
            entry(lastMonth.withDayOfMonth(12), EXPENSE, 55_000, "교통", "교통카드 충전"),
            entry(lastMonth.withDayOfMonth(20), EXPENSE, 89_000, "쇼핑", "운동화"),
            entry(lastMonth.withDayOfMonth(25), INCOME, 150_000, "부수입", "중고거래"),
            // 이번 달
            entry(thisMonth.withDayOfMonth(1), INCOME, 3_000_000, "급여", "월급"),
            entry(thisMonth.withDayOfMonth(1), EXPENSE, 550_000, "주거", "월세"),
            entry(thisMonth.withDayOfMonth(2), EXPENSE, 12_000, "식비", "점심"),
            entry(thisMonth.withDayOfMonth(2), EXPENSE, 4_500, "카페", null),
            entry(thisMonth.withDayOfMonth(5), EXPENSE, 45_000, "식비", "친구랑 저녁"),
            entry(thisMonth.withDayOfMonth(8), EXPENSE, 17_000, "문화", "영화"),
        )

        entryRepository.saveAll(samples)
        log.info("샘플 데이터 {}건을 넣었습니다.", samples.size)
    }
}
