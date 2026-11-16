package com.budgetbook.entry

import com.budgetbook.category.Category
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface EntryRepository : JpaRepository<Entry, Long> {

    /**
     * 최신 거래가 먼저 오도록 날짜 내림차순, 같은 날짜면 나중에 등록한 것부터.
     *
     * `@EntityGraph`: 카테고리를 같은 쿼리에서 JOIN으로 함께 가져온다.
     * 없으면 목록 조회 1번 + 거래 내역마다 카테고리 조회 N번이 실행된다 (N+1 문제).
     */
    @EntityGraph(attributePaths = ["category"])
    fun findAllByOrderByDateDescIdDesc(): List<Entry>

    fun existsByCategory(category: Category): Boolean
}
