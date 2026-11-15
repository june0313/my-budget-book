package com.budgetbook.entry

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDate

/** 가계부의 거래 내역 한 줄 */
@Entity
class Entry(
    /** 거래한 날짜 */
    @Column(nullable = false)
    var date: LocalDate,

    /** 수입/지출 구분 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: EntryType,

    /** 금액(원). 항상 양수이고, 수입인지 지출인지는 type으로 구분한다 */
    @Column(nullable = false)
    var amount: Long,

    /** 카테고리 (예: 식비, 교통, 급여) */
    @Column(nullable = false)
    var category: String,

    /** 메모 (선택) */
    var memo: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
