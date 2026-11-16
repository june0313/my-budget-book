package com.budgetbook.category

import com.budgetbook.entry.EntryType
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/** 404: 요청한 id의 카테고리가 없음 */
class CategoryNotFoundException(id: Long) :
    ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다. id=$id")

/** 409: 같은 구분 안에 같은 이름의 카테고리가 이미 있음 */
class DuplicateCategoryNameException(type: EntryType, name: String) :
    ResponseStatusException(HttpStatus.CONFLICT, "이미 있는 카테고리 이름입니다. (${type.name}, $name)")

/** 409: 거래 내역이 사용 중인 카테고리는 삭제할 수 없음 */
class CategoryInUseException(id: Long) :
    ResponseStatusException(HttpStatus.CONFLICT, "거래 내역에서 사용 중인 카테고리는 삭제할 수 없습니다. id=$id")
