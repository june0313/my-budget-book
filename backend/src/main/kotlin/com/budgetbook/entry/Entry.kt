package com.budgetbook.entry

import com.budgetbook.asset.Asset
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

    /** 구분: 수입 / 저축·투자 / 지출 */
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

    /**
     * 자산: 수입이면 들어온 곳, 지출·저축·투자면 나간 곳 (선택). (P-1)
     * 카테고리와 같은 N:1이지만 `optional = true`(기본값)라 DB 컬럼 asset_id는 null일 수 있다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    var asset: Asset? = null,

    /**
     * 고정 지출인지 (월세, 통신비처럼 매달 정해져 나가는 돈). 지출에만 true일 수 있고, 기본은 변동 지출(false). (E-8)
     * Kotlin에서 `is`로 시작하는 Boolean 속성은 JSON에서도 `isFixed` 그대로 쓰인다 (Category.isDefault와 같다).
     */
    @Column(nullable = false)
    var isFixed: Boolean = false,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
