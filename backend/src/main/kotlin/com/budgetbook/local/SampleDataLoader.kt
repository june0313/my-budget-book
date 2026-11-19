package com.budgetbook.local

import com.budgetbook.asset.AssetRepository
import com.budgetbook.category.CategoryRepository
import com.budgetbook.entry.Entry
import com.budgetbook.entry.EntryRepository
import com.budgetbook.entry.EntryType
import com.budgetbook.entry.EntryType.EXPENSE
import com.budgetbook.entry.EntryType.INCOME
import com.budgetbook.entry.EntryType.SAVING
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
 * - 기본 카테고리와 기본 자산을 쓰므로 DefaultCategoryInitializer(`@Order(1)`), DefaultAssetInitializer(`@Order(2)`) 다음에 실행한다.
 */
@Component
@Profile("local")
@Order(3)
class SampleDataLoader(
    private val entryRepository: EntryRepository,
    private val categoryRepository: CategoryRepository,
    private val assetRepository: AssetRepository,
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

        fun entry(
            date: LocalDate,
            type: EntryType,
            amount: Long,
            categoryName: String,
            memo: String?,
            assetName: String?,
            isFixed: Boolean = false,
        ) = Entry(
            date = date,
            type = type,
            amount = amount,
            category = checkNotNull(categoryRepository.findByTypeAndName(type, categoryName)) {
                "기본 카테고리에 '$categoryName'($type)이 없습니다"
            },
            memo = memo,
            isFixed = isFixed,
            asset = assetName?.let {
                checkNotNull(assetRepository.findByName(it)) { "기본 자산에 '$it'이 없습니다" }
            },
        )

        val samples = listOf(
            // 지난달
            entry(lastMonth.withDayOfMonth(1), INCOME, 3_000_000, "급여", "월급", "은행 계좌"),
            entry(lastMonth.withDayOfMonth(1), EXPENSE, 550_000, "주거", "월세", "은행 계좌", isFixed = true),
            entry(lastMonth.withDayOfMonth(1), SAVING, 500_000, "적금", "청년 적금", "은행 계좌"),
            entry(lastMonth.withDayOfMonth(7), EXPENSE, 32_000, "식비", "장보기", "체크카드"),
            entry(lastMonth.withDayOfMonth(12), EXPENSE, 55_000, "교통", "교통카드 충전", "체크카드"),
            entry(lastMonth.withDayOfMonth(20), EXPENSE, 89_000, "쇼핑", "운동화", "신용카드"),
            entry(lastMonth.withDayOfMonth(25), INCOME, 150_000, "부수입", "중고거래", "현금"),
            // 이번 달
            entry(thisMonth.withDayOfMonth(1), INCOME, 3_200_000, "급여", "월급", "은행 계좌"),
            entry(thisMonth.withDayOfMonth(1), EXPENSE, 600_000, "주거", "월세", "은행 계좌", isFixed = true),
            entry(thisMonth.withDayOfMonth(1), SAVING, 500_000, "적금", "청년 적금", "은행 계좌"),
            entry(thisMonth.withDayOfMonth(1), SAVING, 300_000, "투자", "ETF 적립식", "은행 계좌"),
            entry(thisMonth.withDayOfMonth(2), EXPENSE, 12_000, "식비", "점심", "체크카드"),
            entry(thisMonth.withDayOfMonth(2), EXPENSE, 4_500, "카페", null, "체크카드"),
            entry(thisMonth.withDayOfMonth(3), EXPENSE, 55_000, "통신", "휴대폰 요금", "신용카드", isFixed = true),
            entry(thisMonth.withDayOfMonth(5), EXPENSE, 45_000, "식비", "친구랑 저녁", "신용카드"),
            entry(thisMonth.withDayOfMonth(5), INCOME, 85_000, "부수입", "중고거래", "현금"),
            entry(thisMonth.withDayOfMonth(8), EXPENSE, 17_000, "문화", "영화", "신용카드"),
            entry(thisMonth.withDayOfMonth(10), EXPENSE, 17_000, "문화", "구독 서비스", "신용카드", isFixed = true),
            entry(thisMonth.withDayOfMonth(10), EXPENSE, 14_300, "교통", "택시", null), // 자산은 선택이라 없어도 된다
        )

        entryRepository.saveAll(samples)
        log.info("샘플 데이터 {}건을 넣었습니다.", samples.size)
    }
}
