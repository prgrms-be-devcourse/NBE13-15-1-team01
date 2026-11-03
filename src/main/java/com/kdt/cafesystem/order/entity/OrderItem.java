package com.kdt.cafesystem.order.entity;

import com.kdt.cafesystem.menu.entity.Menu;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 항목. 메뉴명·단가·금액은 접수 당시 값을 스냅샷으로 저장한다.
 * (A09에서 메뉴가 수정돼도 과거 주문·배송 묶음·통계 값은 그대로)
 */
@Entity
@Table(name = "order_items")   // schema.sql 기준
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** 주문 이력이 있는 메뉴는 삭제 불가(MENU_IN_USE) — FK로 보호 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Column(name = "unit_price", nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "line_amount", nullable = false)
    private int lineAmount;

    public static OrderItem of(Menu menu, int quantity) {
        OrderItem item = new OrderItem();
        item.menu = menu;
        item.menuName = menu.getName();
        item.unitPrice = menu.getPrice();
        item.quantity = quantity;
        item.lineAmount = menu.getPrice() * quantity;
        return item;
    }

    void assignOrder(Order order) {
        this.order = order;
    }

    public Long getMenuId() {
        return menu.getId();
    }
}
