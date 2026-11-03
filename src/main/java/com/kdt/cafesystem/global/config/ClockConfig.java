package com.kdt.cafesystem.global.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 서울 시각 기준 Clock.
 * 테스트에서는 Clock.fixed(...)로 바꿔 13:59:59 / 14:00:00 경계를 검증한다.
 */
@Configuration
public class ClockConfig {

    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock clock() {
        return Clock.system(SEOUL);
    }
}
