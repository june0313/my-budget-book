package com.budgetbook.entry

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDate

/** 거래 내역 등록/수정 요청 본문 */
data class EntryRequest(
    val date: LocalDate,
    val type: EntryType,

    @Positive(message = "금액은 0보다 커야 합니다")
    val amount: Long,

    /** 카테고리 목록(GET /api/categories)에서 고른 카테고리의 id */
    val categoryId: Long,

    @Size(max = 255, message = "메모는 255자 이하로 입력해 주세요")
    val memo: String? = null,

    /** 고정 지출인지. 지출(EXPENSE)에만 true를 쓸 수 있다. 빼면 false (변동 지출) */
    val isFixed: Boolean = false,

    /** 자산 목록(GET /api/assets)에서 고른 자산의 id. 빼면 자산 없음 */
    val assetId: Long? = null,
)

/** 거래 내역 응답 본문 */
data class EntryResponse(
    val id: Long,
    val date: LocalDate,
    val type: EntryType,
    val amount: Long,
    val category: CategorySummary,
    val memo: String?,
    val isFixed: Boolean,
    /** 자산이 없으면 null */
    val asset: AssetSummary?,
) {
    /** 거래 내역 응답에 함께 담는 카테고리 요약 */
    data class CategorySummary(
        val id: Long,
        val name: String,
    )

    /** 거래 내역 응답에 함께 담는 자산 요약 */
    data class AssetSummary(
        val id: Long,
        val name: String,
    )

    companion object {
        fun from(entry: Entry) = EntryResponse(
            id = requireNotNull(entry.id) { "저장되지 않은 Entry는 응답으로 변환할 수 없습니다" },
            date = entry.date,
            type = entry.type,
            amount = entry.amount,
            category = CategorySummary(
                id = requireNotNull(entry.category.id),
                name = entry.category.name,
            ),
            memo = entry.memo,
            isFixed = entry.isFixed,
            // ?.let: asset이 null이 아닐 때만 블록을 실행하고, null이면 결과도 null
            asset = entry.asset?.let { AssetSummary(id = requireNotNull(it.id), name = it.name) },
        )
    }
}
