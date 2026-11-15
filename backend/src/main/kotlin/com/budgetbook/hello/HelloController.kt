package com.budgetbook.hello

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
class HelloController {

    @GetMapping("/hello")
    fun hello() = HelloResponse(
        message = "Hello, budget book!",
        currentTime = OffsetDateTime.now(),
        javaVersion = Runtime.version().toString(),
    )
}

data class HelloResponse(
    val message: String,
    /** 서버의 현재 시각 (시간대 오프셋 포함, 예: 2026-11-15T16:40:12.345+09:00) */
    val currentTime: OffsetDateTime,
    /** 서버가 실행 중인 Java 버전 (예: 21.0.5+11) */
    val javaVersion: String,
)
