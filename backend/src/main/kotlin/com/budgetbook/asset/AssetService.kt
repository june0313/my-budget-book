package com.budgetbook.asset

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AssetService(
    private val assetRepository: AssetRepository,
) {

    /** 자산 목록을 등록한 순서대로 */
    fun findAll(): List<AssetResponse> =
        assetRepository.findAllByOrderByIdAsc().map(AssetResponse::from)
}
