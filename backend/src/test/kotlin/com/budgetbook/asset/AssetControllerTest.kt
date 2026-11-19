package com.budgetbook.asset

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/** 앱 전체를 띄우므로 기본 자산이 등록된 상태에서 시작한다 */
@SpringBootTest
@ActiveProfiles("test") // 실행 도구(Gradle/VS Code/IntelliJ)와 관계없이 샘플 데이터 없는 test 프로필로 고정
@AutoConfigureMockMvc
class AssetControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {

    @Test
    fun `기본 자산 목록을 등록한 순서대로 돌려준다`() {
        mockMvc.get("/api/assets").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(DefaultAssetInitializer.DEFAULT_ASSETS.size) }
            jsonPath("$[0].name") { value("현금") }
            jsonPath("$[0].isDefault") { value(true) }
        }
    }
}
