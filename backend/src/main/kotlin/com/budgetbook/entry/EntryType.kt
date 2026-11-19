package com.budgetbook.entry

/** 거래 구분 */
enum class EntryType {
    /** 수입 */
    INCOME,

    /** 저축·투자: 적금, 투자, 연금처럼 계획해서 떼어 두는 돈. 소비가 아니므로 지출과 따로 집계한다 (E-7) */
    SAVING,

    /** 지출 */
    EXPENSE,
}
