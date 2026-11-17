package com.budgetbook.entry

import com.budgetbook.category.Category
import com.budgetbook.category.CategoryRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth

@Service
@Transactional(readOnly = true)
class EntryService(
    private val entryRepository: EntryRepository,
    private val categoryRepository: CategoryRepository,
) {

    /** 한 달(1일 ~ 말일)의 거래 내역을 최신순으로 가져온다 (S-1) */
    fun findAllByMonth(month: YearMonth): List<EntryResponse> =
        entryRepository.findAllByDateBetweenOrderByDateDescIdDesc(month.atDay(1), month.atEndOfMonth())
            .map(EntryResponse::from)

    fun findById(id: Long): EntryResponse =
        EntryResponse.from(getEntry(id))

    @Transactional
    fun create(request: EntryRequest): EntryResponse {
        val entry = Entry(
            date = request.date,
            type = request.type,
            amount = request.amount,
            category = getCategoryFor(request),
            memo = request.memo,
        )
        return EntryResponse.from(entryRepository.save(entry))
    }

    @Transactional
    fun update(id: Long, request: EntryRequest): EntryResponse {
        val entry = getEntry(id)
        entry.date = request.date
        entry.type = request.type
        entry.amount = request.amount
        entry.category = getCategoryFor(request)
        entry.memo = request.memo
        // save()를 호출하지 않아도 트랜잭션이 끝날 때 변경된 값이 UPDATE 된다 (변경 감지)
        return EntryResponse.from(entry)
    }

    @Transactional
    fun delete(id: Long) {
        entryRepository.delete(getEntry(id))
    }

    private fun getEntry(id: Long): Entry =
        entryRepository.findByIdOrNull(id) ?: throw EntryNotFoundException(id)

    /** 요청의 카테고리가 존재하고, 거래 내역과 같은 구분(수입/지출)인지 확인한다 (C-7) */
    private fun getCategoryFor(request: EntryRequest): Category {
        val category = categoryRepository.findByIdOrNull(request.categoryId)
            ?: throw InvalidEntryCategoryException("존재하지 않는 카테고리입니다. categoryId=${request.categoryId}")
        if (category.type != request.type) {
            throw InvalidEntryCategoryException(
                "거래 구분과 카테고리 구분이 다릅니다. (거래: ${request.type}, 카테고리 '${category.name}': ${category.type})"
            )
        }
        return category
    }
}
