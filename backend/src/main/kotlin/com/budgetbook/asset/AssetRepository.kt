package com.budgetbook.asset

import org.springframework.data.jpa.repository.JpaRepository

interface AssetRepository : JpaRepository<Asset, Long> {

    fun findAllByOrderByIdAsc(): List<Asset>

    fun findByName(name: String): Asset?

    fun existsByIsDefaultTrue(): Boolean
}
