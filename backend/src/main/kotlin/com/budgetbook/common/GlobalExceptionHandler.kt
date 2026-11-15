package com.budgetbook.common

import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import tools.jackson.core.JacksonException
import tools.jackson.core.exc.StreamReadException
import tools.jackson.databind.exc.InvalidFormatException
import tools.jackson.databind.exc.InvalidNullException
import tools.jackson.databind.exc.MismatchedInputException

/** 에러 응답의 `errors` 배열에 들어가는 항목: 어느 필드가 왜 잘못됐는지 */
data class FieldErrorDetail(
    val field: String,
    val message: String,
)

/**
 * 모든 컨트롤러에서 발생한 예외를 한곳에서 처리한다.
 *
 * Spring이 제공하는 [ResponseEntityExceptionHandler]를 상속하면 Spring MVC의 표준 예외들이
 * 이미 ProblemDetail(RFC 9457) 형식으로 변환된다. 여기서는 그중 입력값 관련 400 응답만
 * 원인을 알 수 있도록 메시지를 바꾼다.
 */
@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    /** @Valid 검증 실패 (예: amount가 0 이하, category가 빈 문자열) */
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val errors = ex.bindingResult.fieldErrors.map {
            FieldErrorDetail(field = it.field, message = it.defaultMessage ?: "올바르지 않은 값입니다")
        }
        val problem = ex.body.apply {
            detail = "입력값이 올바르지 않습니다"
            setProperty("errors", errors)
        }
        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    /** 요청 본문을 객체로 바꾸지 못함 (본문 없음, JSON 문법 오류, 필수 값 누락, 형식 오류) */
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val problem = when (val cause = ex.cause) {
            is InvalidNullException -> invalidInput(status, cause, "필수 값입니다")
            is InvalidFormatException -> invalidInput(status, cause, formatErrorMessage(cause))
            is MismatchedInputException -> invalidInput(status, cause, "값의 형식이 올바르지 않습니다")
            is StreamReadException -> ProblemDetail.forStatusAndDetail(status, "JSON 문법이 올바르지 않습니다")
            null -> ProblemDetail.forStatusAndDetail(status, "요청 본문이 비어 있습니다")
            else -> ProblemDetail.forStatusAndDetail(status, "요청 본문을 읽을 수 없습니다")
        }
        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    /** 경로/쿼리 파라미터 타입 불일치 (예: /api/entries/abc) */
    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val problem = ProblemDetail.forStatusAndDetail(status, "입력값이 올바르지 않습니다").apply {
            setProperty(
                "errors",
                listOf(FieldErrorDetail(ex.propertyName ?: "unknown", "값의 형식이 올바르지 않습니다 (입력값: ${ex.value})")),
            )
        }
        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    private fun invalidInput(status: HttpStatusCode, cause: JacksonException, message: String) =
        ProblemDetail.forStatusAndDetail(status, "입력값이 올바르지 않습니다").apply {
            setProperty("errors", listOf(FieldErrorDetail(fieldPath(cause), message)))
        }

    /** 예: "type" 필드에 "FOOD"를 보내면 → 허용 값 목록까지 알려준다 */
    private fun formatErrorMessage(cause: InvalidFormatException): String {
        val allowed = cause.targetType?.enumConstants
        return if (allowed != null) {
            "허용되지 않는 값입니다 (입력값: ${cause.value}, 허용값: ${allowed.joinToString()})"
        } else {
            "값의 형식이 올바르지 않습니다 (입력값: ${cause.value})"
        }
    }

    /** Jackson 예외에 담긴 경로를 "items[0].name" 같은 문자열로 만든다 */
    private fun fieldPath(cause: JacksonException): String =
        cause.path.joinToString(".") { it.propertyName ?: "[${it.index}]" }
            .replace(".[", "[")
            .ifEmpty { "unknown" }
}
