package com.budgetbook.entry

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.YearMonth

@RestController
@RequestMapping("/api/entries")
class EntryController(
    private val entryService: EntryService,
) {

    /**
     * GET /api/entries?month=2026-11 → 2026년 11월 거래 내역
     * GET /api/entries                → 이번 달 거래 내역
     *
     * "2026-11" 문자열은 Spring이 YearMonth로 바꿔 준다. 형식이 틀리면(예: 2026-13)
     * 변환에 실패해 GlobalExceptionHandler.handleTypeMismatch가 400으로 응답한다.
     */
    @GetMapping
    fun findAllByMonth(@RequestParam(required = false) month: YearMonth?): List<EntryResponse> =
        entryService.findAllByMonth(month ?: YearMonth.now())

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): EntryResponse = entryService.findById(id)

    @PostMapping
    fun create(@Valid @RequestBody request: EntryRequest): ResponseEntity<EntryResponse> {
        val created = entryService.create(request)
        // 201 Created + Location: /api/entries/{새 id}
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id)
            .toUri()
        return ResponseEntity.created(location).body(created)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: EntryRequest,
    ): EntryResponse = entryService.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = entryService.delete(id)
}
