package com.budgetbook.asset

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/assets")
class AssetController(
    private val assetService: AssetService,
) {

    /** GET /api/assets: 거래 내역에 고를 수 있는 자산 목록 (추가·수정·삭제는 P-2에서) */
    @GetMapping
    fun findAll(): List<AssetResponse> = assetService.findAll()
}
