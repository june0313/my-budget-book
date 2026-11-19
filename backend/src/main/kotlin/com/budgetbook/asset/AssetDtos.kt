package com.budgetbook.asset

data class AssetResponse(
    val id: Long,
    val name: String,
    val isDefault: Boolean,
) {
    companion object {
        fun from(asset: Asset) = AssetResponse(
            id = requireNotNull(asset.id),
            name = asset.name,
            isDefault = asset.isDefault,
        )
    }
}
