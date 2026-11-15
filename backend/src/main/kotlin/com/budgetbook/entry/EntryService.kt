package com.budgetbook.entry

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class EntryService(
    private val entryRepository: EntryRepository,
) {

    fun findAll(): List<EntryResponse> =
        entryRepository.findAllByOrderByDateDescIdDesc().map(EntryResponse::from)

    fun findById(id: Long): EntryResponse =
        EntryResponse.from(getEntry(id))

    @Transactional
    fun create(request: EntryRequest): EntryResponse {
        val entry = Entry(
            date = request.date,
            type = request.type,
            amount = request.amount,
            category = request.category,
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
        entry.category = request.category
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
}
