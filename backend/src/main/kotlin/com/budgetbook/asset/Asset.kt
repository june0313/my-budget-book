package com.budgetbook.asset

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

/**
 * 자산: 돈이 들어오거나 나간 곳 (현금, 은행 계좌, 카드 등). (P-1)
 *
 * 거래 내역은 수입이면 "들어온 곳", 지출·저축·투자면 "나간 곳"으로 자산을 가리킨다.
 * 지금은 기본 자산만 있고, 사용자가 직접 추가·수정·삭제하는 기능(P-2)은 나중에 만든다.
 */
@Entity
class Asset(
    /** 이름은 중복될 수 없다 (DB 유니크 제약) */
    @Column(nullable = false, unique = true, length = 20)
    var name: String,

    /** 앱이 기본으로 제공한 자산인지 (false면 사용자가 직접 추가한 것. P-2에서 쓴다) */
    @Column(nullable = false, updatable = false)
    val isDefault: Boolean = false,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
