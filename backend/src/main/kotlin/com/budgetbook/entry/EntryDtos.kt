package com.budgetbook.entry

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.LocalDate

/** 거래 내역 등록/수정 요청 본문 */
data class EntryRequest(
    val date: LocalDate,
    val type: EntryType,

    @Positive(message = "금액은 0보다 커야 합니다")
    val amount: Long,

    @NotBlank(message = "카테고리를 입력해 주세요")
    @Size(max = 50, message = "카테고리는 50자 이하로 입력해 주세요")
    val category: String,

    @Size(max = 255, message = "메모는 255자 이하로 입력해 주세요")
    val memo: String? = null,
)

/** 거래 내역 응답 본문 */
data class EntryResponse(
    val id: Long,
    val date: LocalDate,
    val type: EntryType,
    val amount: Long,
    val category: String,
    val memo: String?,
) {
    companion object {
        fun from(entry: Entry) = EntryResponse(
            id = requireNotNull(entry.id) { "저장되지 않은 Entry는 응답으로 변환할 수 없습니다" },
            date = entry.date,
            type = entry.type,
            amount = entry.amount,
            category = entry.category,
            memo = entry.memo,
        )
    }
}
