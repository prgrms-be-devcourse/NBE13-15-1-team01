package com.kdt.cafesystem.order.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문. order는 SQL 예약어라 테이블명은 orders.
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_email_shipping_date", columnList = "email, shipping_date"),
        @Index(name = "idx_orders_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 대소문자 구분(utf8mb4_bin) */
    @Column(nullable = false, length = 100,
            columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin")
    private String email;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 5)
    private String postcode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "shipping_date", nullable = false)
    private LocalDate shippingDate;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /** 공백·줄바꿈 원본 보존. schema.sql: NOT NULL DEFAULT '' */
    @Column(nullable = false, length = 200)
    private String memo;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    public static Order create(String email, String address, String postcode, String memo,
                               LocalDateTime createdAt, LocalDate shippingDate) {
        Order order = new Order();
        order.email = email;
        order.address = address;
        order.postcode = postcode;
        order.memo = memo == null ? "" : memo;   // 메모 없으면 빈 문자열
        order.createdAt = createdAt;
        order.shippingDate = shippingDate;
        order.status = OrderStatus.PLACED;
        order.totalAmount = 0;
        return order;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.assignOrder(this);
        totalAmount += item.getLineAmount();
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    public void cancel(LocalDateTime now) {
        this.status = OrderStatus.CANCELLED;
        this.cancelledAt = now;
    }

    public void changeMemo(String memo) {
        this.memo = memo == null ? "" : memo;
    }

    /** 같은 배송지인지 (주소·우편번호 모두 일치) */
    public boolean hasSameDestination(String address, String postcode) {
        return this.address.equals(address) && this.postcode.equals(postcode);
    }
}
