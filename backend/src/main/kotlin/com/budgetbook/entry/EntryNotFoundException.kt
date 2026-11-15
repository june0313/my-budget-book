package com.budgetbook.entry

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/** 요청한 id의 거래 내역이 없을 때 던지는 예외. 응답은 404 Not Found가 된다 */
class EntryNotFoundException(id: Long) :
    ResponseStatusException(HttpStatus.NOT_FOUND, "거래 내역을 찾을 수 없습니다. id=$id")
