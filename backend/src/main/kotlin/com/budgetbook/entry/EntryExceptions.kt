package com.budgetbook.entry

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

/** 요청한 id의 거래 내역이 없을 때 던지는 예외. 응답은 404 Not Found가 된다 */
class EntryNotFoundException(id: Long) :
    ResponseStatusException(HttpStatus.NOT_FOUND, "거래 내역을 찾을 수 없습니다. id=$id")

/**
 * 거래 내역 요청의 카테고리가 올바르지 않을 때 (없는 카테고리, 구분 불일치). 응답은 400 Bad Request.
 * 주소(/api/entries/{id})의 대상이 없는 게 아니라 요청 본문의 값이 잘못된 것이므로 404가 아니라 400이다.
 */
class InvalidEntryCategoryException(message: String) :
    ResponseStatusException(HttpStatus.BAD_REQUEST, message)

/** 400: 요청의 자산 id가 없는 자산일 때 */
class InvalidEntryAssetException(assetId: Long) :
    ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 자산입니다. assetId=$assetId")

/** 400: 지출이 아닌 거래(수입, 저축·투자)를 고정 지출로 표시했을 때 */
class FixedOnlyForExpenseException(type: EntryType) :
    ResponseStatusException(HttpStatus.BAD_REQUEST, "고정 지출 표시는 지출에만 쓸 수 있습니다. (거래: $type)")
