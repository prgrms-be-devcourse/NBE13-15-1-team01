package com.kdt.cafesystem.order.repository;

import com.kdt.cafesystem.order.entity.Order;
import com.kdt.cafesystem.order.entity.OrderStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
/* 임포트 추가 */
import org.springframework.data.jpa.repository.EntityGraph;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** A12 이메일별 주문 목록 (정렬 기준은 명세서 확인 후 조정) */
    List<Order> findByEmailOrderByIdDesc(String email);

    /** A11·A23 배송지 일치 검사용: 같은 이메일·배송일의 접수 상태 주문 */
    Optional<Order> findFirstByEmailAndShippingDateAndStatus(
            String email, LocalDate shippingDate, OrderStatus status);

    /* 추가한 조회 메서드 배송일이랑 상태가 같은 주문을 조회 */
    @EntityGraph(attributePaths = "items")
    List<Order> findByShippingDateAndStatusOrderByIdAsc(LocalDate shippingDate, OrderStatus status);
}
