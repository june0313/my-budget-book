package com.budgetbook.entry

import com.budgetbook.category.Category
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
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

    /**
     * 카테고리. 거래 내역 여러 개가 카테고리 하나를 가리킨다(N:1).
     * DB에는 category_id 외래키 컬럼으로 저장된다.
     * LAZY: 거래 내역을 조회할 때 카테고리는 실제로 쓸 때 가져온다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    /** 메모 (선택) */
    var memo: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
