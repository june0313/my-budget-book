package com.budgetbook.category

import com.budgetbook.entry.EntryType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

private const val NAME_BLANK_MESSAGE = "카테고리 이름을 입력해 주세요"
private const val NAME_SIZE_MESSAGE = "카테고리 이름은 20자 이하로 입력해 주세요"

/** 카테고리 추가 요청 */
data class CategoryCreateRequest(
    @NotBlank(message = NAME_BLANK_MESSAGE)
    @Size(max = 20, message = NAME_SIZE_MESSAGE)
    val name: String,

    val type: EntryType,
)

/** 카테고리 이름 변경 요청. 구분(type)은 바꿀 수 없으므로 받지 않는다 */
data class CategoryRenameRequest(
    @NotBlank(message = NAME_BLANK_MESSAGE)
    @Size(max = 20, message = NAME_SIZE_MESSAGE)
    val name: String,
)

data class CategoryResponse(
    val id: Long,
    val name: String,
    val type: EntryType,
    val isDefault: Boolean,
) {
    companion object {
        fun from(category: Category) = CategoryResponse(
            id = requireNotNull(category.id),
            name = category.name,
            type = category.type,
            isDefault = category.isDefault,
        )
    }
}
