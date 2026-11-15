package com.budgetbook.local

import com.budgetbook.entry.Entry
import com.budgetbook.entry.EntryRepository
import com.budgetbook.entry.EntryType.EXPENSE
import com.budgetbook.entry.EntryType.INCOME
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * 로컬 개발용 샘플 데이터를 넣는다.
 *
 * - `local` 프로필에서만 동작한다. (application.properties에서 기본 프로필을 local로 지정)
 * - 날짜는 실행한 날 기준 이번 달/지난달로 만들어서, 언제 실행해도 "최근 데이터"가 있다.
 * - 이미 데이터가 있으면 넣지 않는다. (나중에 데이터가 유지되는 DB로 바꿔도 중복되지 않도록)
 */
@Component
@Profile("local")
class SampleDataLoader(
    private val entryRepository: EntryRepository,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        if (entryRepository.count() > 0) {
            log.info("이미 데이터가 있어 샘플 데이터를 넣지 않습니다.")
            return
        }

        val thisMonth = LocalDate.now().withDayOfMonth(1)
        val lastMonth = thisMonth.minusMonths(1)

        val samples = listOf(
            // 지난달
            Entry(lastMonth.withDayOfMonth(1), INCOME, 3_000_000, "급여", "월급"),
            Entry(lastMonth.withDayOfMonth(3), EXPENSE, 550_000, "주거", "월세"),
            Entry(lastMonth.withDayOfMonth(7), EXPENSE, 32_000, "식비", "장보기"),
            Entry(lastMonth.withDayOfMonth(12), EXPENSE, 55_000, "교통", "교통카드 충전"),
            Entry(lastMonth.withDayOfMonth(20), EXPENSE, 89_000, "쇼핑", "운동화"),
            Entry(lastMonth.withDayOfMonth(25), INCOME, 150_000, "부수입", "중고거래"),
            // 이번 달
            Entry(thisMonth.withDayOfMonth(1), INCOME, 3_000_000, "급여", "월급"),
            Entry(thisMonth.withDayOfMonth(1), EXPENSE, 550_000, "주거", "월세"),
            Entry(thisMonth.withDayOfMonth(2), EXPENSE, 12_000, "식비", "점심"),
            Entry(thisMonth.withDayOfMonth(2), EXPENSE, 4_500, "카페", null),
            Entry(thisMonth.withDayOfMonth(5), EXPENSE, 45_000, "식비", "친구랑 저녁"),
            Entry(thisMonth.withDayOfMonth(8), EXPENSE, 17_000, "문화", "영화"),
        )

        entryRepository.saveAll(samples)
        log.info("샘플 데이터 {}건을 넣었습니다.", samples.size)
    }
}
