package com.budgetbook.asset

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 기본 자산을 등록한다. DefaultCategoryInitializer와 같은 방식이다.
 *
 * 앱의 기능이므로 프로필과 관계없이 항상 동작하고, 기본 자산이 이미 있으면 아무것도 하지 않는다.
 * 자산을 직접 관리하는 기능(P-2)이 생기기 전까지는 이 목록만 쓸 수 있다.
 *
 * `@Order(2)`: 기본 카테고리(1) 다음, 샘플 데이터(3) 전에 실행한다.
 */
@Component
@Order(2)
class DefaultAssetInitializer(
    private val assetRepository: AssetRepository,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun run(args: ApplicationArguments) {
        if (assetRepository.existsByIsDefaultTrue()) return

        val defaults = DEFAULT_ASSETS.map { Asset(name = it, isDefault = true) }
        assetRepository.saveAll(defaults)
        log.info("기본 자산 {}개를 등록했습니다.", defaults.size)
    }

    companion object {
        val DEFAULT_ASSETS = listOf("현금", "은행 계좌", "신용카드", "체크카드")
    }
}
