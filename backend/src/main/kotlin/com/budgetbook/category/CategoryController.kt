package com.budgetbook.category

import com.budgetbook.entry.EntryType
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/categories")
class CategoryController(
    private val categoryService: CategoryService,
) {

    /** GET /api/categories, GET /api/categories?type=EXPENSE */
    @GetMapping
    fun findAll(@RequestParam(required = false) type: EntryType?): List<CategoryResponse> =
        categoryService.findAll(type)

    @PostMapping
    fun create(@Valid @RequestBody request: CategoryCreateRequest): ResponseEntity<CategoryResponse> {
        val created = categoryService.create(request)
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id)
            .toUri()
        return ResponseEntity.created(location).body(created)
    }

    /** 이름만 바꾸는 부분 수정이라 PUT이 아니라 PATCH */
    @PatchMapping("/{id}")
    fun rename(
        @PathVariable id: Long,
        @Valid @RequestBody request: CategoryRenameRequest,
    ): CategoryResponse = categoryService.rename(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = categoryService.delete(id)
}
