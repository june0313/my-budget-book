package com.budgetbook.category

import com.budgetbook.entry.EntryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * 거래 내역의 분류. 구분(수입, 저축·투자, 지출)마다 따로 있다.
 *
 * - 같은 구분(type) 안에서 이름은 중복될 수 없다. (DB 유니크 제약으로도 보장)
 * - 구분은 만든 뒤 바꿀 수 없다. 이미 이 카테고리를 쓰는 거래 내역과 어긋나기 때문.
 */
@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["type", "name"])])
class Category(
    @Column(nullable = false, length = 20)
    var name: String,

    /** 구분(수입 / 저축·투자 / 지출). `val`이라 코드에서 바꿀 수 없고, `updatable = false`라 DB에서도 UPDATE 대상이 아니다 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val type: EntryType,

    /** 앱이 기본으로 제공한 카테고리인지 (false면 사용자가 직접 추가한 것) */
    @Column(nullable = false, updatable = false)
    val isDefault: Boolean = false,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set
}
