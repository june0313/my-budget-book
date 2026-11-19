package com.budgetbook.entry

import com.budgetbook.asset.Asset
import com.budgetbook.asset.AssetRepository
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
    private val assetRepository: AssetRepository,
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
            isFixed = getIsFixedFor(request),
            asset = getAssetFor(request),
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
        entry.isFixed = getIsFixedFor(request)
        entry.asset = getAssetFor(request)
        // save()를 호출하지 않아도 트랜잭션이 끝날 때 변경된 값이 UPDATE 된다 (변경 감지)
        return EntryResponse.from(entry)
    }

    @Transactional
    fun delete(id: Long) {
        entryRepository.delete(getEntry(id))
    }

    private fun getEntry(id: Long): Entry =
        entryRepository.findByIdOrNull(id) ?: throw EntryNotFoundException(id)

    /** 자산은 선택이다. 보냈다면 존재하는 자산이어야 한다 (P-1) */
    private fun getAssetFor(request: EntryRequest): Asset? {
        // assetId가 null이면 여기서 바로 null을 돌려준다 (?: return null)
        val assetId = request.assetId ?: return null
        return assetRepository.findByIdOrNull(assetId) ?: throw InvalidEntryAssetException(assetId)
    }

    /** 고정 지출 표시는 지출에만 허용한다 (E-8) */
    private fun getIsFixedFor(request: EntryRequest): Boolean {
        if (request.isFixed && request.type != EntryType.EXPENSE) {
            throw FixedOnlyForExpenseException(request.type)
        }
        return request.isFixed
    }

    /** 요청의 카테고리가 존재하고, 거래 내역과 같은 구분인지 확인한다 (C-7) */
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
