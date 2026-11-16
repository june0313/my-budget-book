package com.budgetbook.category

import com.budgetbook.entry.EntryType
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<Category, Long> {

    fun findAllByOrderByTypeAscIdAsc(): List<Category>

    fun findAllByTypeOrderByIdAsc(type: EntryType): List<Category>

    fun findByTypeAndName(type: EntryType, name: String): Category?

    fun existsByTypeAndName(type: EntryType, name: String): Boolean

    /** 이름 변경 시 중복 검사용: 자기 자신은 빼고 같은 이름이 있는지 */
    fun existsByTypeAndNameAndIdNot(type: EntryType, name: String, id: Long): Boolean

    fun existsByIsDefaultTrue(): Boolean
}
