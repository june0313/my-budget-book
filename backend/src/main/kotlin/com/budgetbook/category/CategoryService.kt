package com.budgetbook.category

import com.budgetbook.entry.EntryRepository
import com.budgetbook.entry.EntryType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val entryRepository: EntryRepository,
) {

    /** type이 null이면 전체, 아니면 해당 구분만 */
    fun findAll(type: EntryType?): List<CategoryResponse> {
        val categories = if (type == null) {
            categoryRepository.findAllByOrderByTypeAscIdAsc()
        } else {
            categoryRepository.findAllByTypeOrderByIdAsc(type)
        }
        return categories.map(CategoryResponse::from)
    }

    @Transactional
    fun create(request: CategoryCreateRequest): CategoryResponse {
        val name = request.name.trim()
        if (categoryRepository.existsByTypeAndName(request.type, name)) {
            throw DuplicateCategoryNameException(request.type, name)
        }
        val category = categoryRepository.save(Category(name = name, type = request.type))
        return CategoryResponse.from(category)
    }

    @Transactional
    fun rename(id: Long, request: CategoryRenameRequest): CategoryResponse {
        val category = getCategory(id)
        val name = request.name.trim()
        if (categoryRepository.existsByTypeAndNameAndIdNot(category.type, name, id)) {
            throw DuplicateCategoryNameException(category.type, name)
        }
        category.name = name
        return CategoryResponse.from(category)
    }

    @Transactional
    fun delete(id: Long) {
        val category = getCategory(id)
        if (entryRepository.existsByCategory(category)) {
            throw CategoryInUseException(id)
        }
        categoryRepository.delete(category)
    }

    fun getCategory(id: Long): Category =
        categoryRepository.findByIdOrNull(id) ?: throw CategoryNotFoundException(id)
}
