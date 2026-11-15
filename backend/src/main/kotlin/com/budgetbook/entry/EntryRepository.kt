package com.budgetbook.entry

import org.springframework.data.jpa.repository.JpaRepository

interface EntryRepository : JpaRepository<Entry, Long> {

    /** 최신 거래가 먼저 오도록 날짜 내림차순, 같은 날짜면 나중에 등록한 것부터 */
    fun findAllByOrderByDateDescIdDesc(): List<Entry>
}
