package com.budgetbook.entry

import com.budgetbook.category.Category
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface EntryRepository : JpaRepository<Entry, Long> {

    /**
     * [start] ~ [end] 날짜(양 끝 포함)의 거래 내역을 최신순으로 가져온다.
     *
     * 메서드 이름으로 쿼리가 만들어진다 (파생 쿼리):
     * `findAllBy` + `DateBetween`(where date between ? and ?) + `OrderByDateDescIdDesc`
     * → 날짜 내림차순, 같은 날짜면 나중에 등록한 것부터.
     *
     * `@EntityGraph`: 카테고리를 같은 쿼리에서 JOIN으로 함께 가져온다.
     * 없으면 목록 조회 1번 + 거래 내역마다 카테고리 조회 N번이 실행된다 (N+1 문제).
     */
    @EntityGraph(attributePaths = ["category"])
    fun findAllByDateBetweenOrderByDateDescIdDesc(start: LocalDate, end: LocalDate): List<Entry>

    fun existsByCategory(category: Category): Boolean
}
