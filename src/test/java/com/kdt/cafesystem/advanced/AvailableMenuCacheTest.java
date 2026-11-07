package com.kdt.cafesystem.advanced;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.kdt.cafesystem.category.entity.Category;
import com.kdt.cafesystem.category.repository.CategoryRepo;
import com.kdt.cafesystem.menu.dto.MenuAvailabilityRequestDto;
import com.kdt.cafesystem.menu.dto.MenuRequestDto;
import com.kdt.cafesystem.menu.dto.MenuResponseDto;
import com.kdt.cafesystem.menu.repository.MenuRepo;
import com.kdt.cafesystem.menu.service.AvailableMenuCache;
import com.kdt.cafesystem.menu.service.MenuService;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;


// 선택 심화: A19 판매 중 메뉴 Redis 캐시를 실제 Redis와 실제 JPA로 확인
// DB는 테스트용 H2(MySQL 호환 모드), Redis는 localhost:6379의 1번 DB를 사용
// Redis가 꺼져 있으면 실패 대신 건너뛴다
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:redis-cache-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.data.redis.database=1"
})
class AvailableMenuCacheTest {

    @Autowired
    MenuService menuService;

    @Autowired
    MenuRepo menuRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    Statistics statistics;
    Long categoryId;
    Long menuId;

    @BeforeEach
    void setUp() {
        assumeTrue(redisRunning(), "Redis(localhost:6379)가 꺼져 있어 캐시 테스트를 건너뜁니다");

        menuRepo.deleteAll();
        categoryRepo.deleteAll();
        categoryId = categoryRepo.save(new Category("싱글오리진")).getId();
        menuId = menuService.create(new MenuRequestDto(categoryId, "Ethiopia Sidamo", 11000, "")).id();

        redisTemplate.delete(AvailableMenuCache.KEY);
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    @Test
    @DisplayName("첫 조회는 DB, 캐시가 살아 있는 동안 다시 조회하면 DB를 조회하지 않는다")
    void secondCallUsesCache() {
        List<MenuResponseDto> first = menuService.findAvailable();
        long queriesAfterFirst = statistics.getPrepareStatementCount();

        List<MenuResponseDto> second = menuService.findAvailable();

        assertThat(queriesAfterFirst).isEqualTo(1);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(queriesAfterFirst);
        assertThat(second).isEqualTo(first);

        assertThat(redisTemplate.opsForValue().get(AvailableMenuCache.KEY)).contains("Ethiopia Sidamo");
        Long ttl = redisTemplate.getExpire(AvailableMenuCache.KEY);
        assertThat(ttl).isBetween(1L, AvailableMenuCache.TTL.toSeconds());
    }

    @Test
    @DisplayName("메뉴 등록 후 첫 조회에 새 메뉴가 보인다")
    void createEvictsCache() {
        menuService.findAvailable();

        menuService.create(new MenuRequestDto(categoryId, "Brazil Serra Do Caparaó", 7000, ""));

        assertThat(names(menuService.findAvailable())).containsExactly("Ethiopia Sidamo", "Brazil Serra Do Caparaó");
    }

    @Test
    @DisplayName("이름·가격·카테고리 수정 후 첫 조회에 바뀐 값이 보인다")
    void updateEvictsCache() {
        Long otherCategoryId = categoryRepo.save(new Category("블렌드")).getId();
        menuService.findAvailable();

        menuService.update(menuId, new MenuRequestDto(otherCategoryId, "House Blend", 9000, ""));

        MenuResponseDto menu = menuService.findAvailable().get(0);
        assertThat(menu.name()).isEqualTo("House Blend");
        assertThat(menu.price()).isEqualTo(9000);
        assertThat(menu.categoryId()).isEqualTo(otherCategoryId);
    }

    @Test
    @DisplayName("주문 이력 없는 메뉴 삭제 후 첫 조회에서 빠진다")
    void deleteEvictsCache() {
        menuService.findAvailable();

        menuService.delete(menuId);

        assertThat(menuService.findAvailable()).isEmpty();
    }

    @Test
    @DisplayName("판매 중지 후 첫 조회에서 빠지고, 재개 후 첫 조회에 다시 보인다")
    void availabilityEvictsCache() {
        menuService.findAvailable();

        menuService.changeAvailability(menuId, new MenuAvailabilityRequestDto(false));
        assertThat(menuService.findAvailable()).isEmpty();

        menuService.changeAvailability(menuId, new MenuAvailabilityRequestDto(true));
        assertThat(names(menuService.findAvailable())).containsExactly("Ethiopia Sidamo");
    }

    private List<String> names(List<MenuResponseDto> menus) {
        return menus.stream().map(MenuResponseDto::name).toList();
    }

    private boolean redisRunning() {
        try {
            return "PONG".equals(redisTemplate.getConnectionFactory().getConnection().ping());
        } catch (RuntimeException e) {
            return false;
        }
    }
}