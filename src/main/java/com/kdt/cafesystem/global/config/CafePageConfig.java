package com.kdt.cafesystem.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

/**
 * 선택 심화: 메뉴 페이징 기본값. page·size가 없으면 0·10, size는 최대 50으로 제한한다.
 */
@Configuration
@EnableSpringDataWebSupport
public class CafePageConfig {

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
        return resolver -> {
            resolver.setFallbackPageable(PageRequest.of(0, 10));
            resolver.setMaxPageSize(50);
        };
    }
}
