package com.kdt.cafesystem.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CafeCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 기존 /api/** 경로에만 CORS 규칙을 적용한다. 새 API를 만드는 설정은 아니다.
        registry.addMapping("/api/**")
                // HTML 화면을 연 출처만 허용한다. /index.html 같은 경로는 넣지 않는다.
                .allowedOrigins("http://127.0.0.1:8000")
                // 브라우저가 실제 요청에 사용할 메서드와 OPTIONS를 명시한다.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // JSON 요청 본문의 형식을 알리는 Content-Type 헤더를 허용한다.
                .allowedHeaders("Content-Type");
    }
}
