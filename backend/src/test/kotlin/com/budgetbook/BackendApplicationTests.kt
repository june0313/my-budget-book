package com.budgetbook

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test") // 실행 도구(Gradle/VS Code/IntelliJ)와 관계없이 샘플 데이터 없는 test 프로필로 고정
class BackendApplicationTests {

	@Test
	fun contextLoads() {
	}

}
