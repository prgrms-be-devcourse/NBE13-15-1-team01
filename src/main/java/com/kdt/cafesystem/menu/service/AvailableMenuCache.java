package com.kdt.cafesystem.menu.service;

import com.kdt.cafesystem.menu.dto.MenuResponseDto;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * 선택 심화: A19 판매 중 메뉴 Redis 캐시.
 * Redis 오류가 나도 예외를 던지지 않고, 호출한 쪽이 DB에서 조회하도록 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AvailableMenuCache {

    public static final String KEY = "cafe:menus:available";
    public static final Duration TTL = Duration.ofSeconds(60);

    private static final TypeReference<List<MenuResponseDto>> MENU_LIST = new TypeReference<>() {
    };

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** 캐시에 있으면 목록, 없거나 Redis 오류면 빈 Optional */
    public Optional<List<MenuResponseDto>> get() {
        try {
            String json = redisTemplate.opsForValue().get(KEY);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, MENU_LIST));
        } catch (RuntimeException e) {
            log.warn("Redis 캐시 조회 실패, DB에서 조회합니다: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** 목록을 JSON으로 바꿔 TTL과 함께 저장 */
    public void put(List<MenuResponseDto> menus) {
        try {
            redisTemplate.opsForValue().set(KEY, objectMapper.writeValueAsString(menus), TTL);
        } catch (RuntimeException e) {
            log.warn("Redis 캐시 저장 실패: {}", e.getMessage());
        }
    }

    /** 메뉴가 바뀌면 호출. DB 변경이 확정(commit)된 뒤에 지운다 */
    public void evict() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    delete();
                }
            });
            return;
        }
        delete();
    }

    private void delete() {
        try {
            redisTemplate.delete(KEY);
        } catch (RuntimeException e) {
            log.warn("Redis 캐시 삭제 실패, TTL이 지나면 사라집니다: {}", e.getMessage());
        }
    }
}
