package com.kdt.cafesystem.order.service;

import com.kdt.cafesystem.global.exception.BusinessException;
import com.kdt.cafesystem.global.exception.ErrorCode;
import com.kdt.cafesystem.menu.entity.Menu;
import com.kdt.cafesystem.menu.repository.MenuRepo;
import com.kdt.cafesystem.order.dto.MemoUpdateRequest;
import com.kdt.cafesystem.order.dto.OrderCreateRequest;
import com.kdt.cafesystem.order.dto.OrderResponse;
import com.kdt.cafesystem.order.entity.Order;
import com.kdt.cafesystem.order.entity.OrderItem;
import com.kdt.cafesystem.order.entity.OrderStatus;
import com.kdt.cafesystem.order.repository.OrderRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepo menuRepository;
    private final ShippingPolicy shippingPolicy;
    private final Clock clock;

    /** A11 주문 접수 */
    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        List<Line> lines = request.items().stream()
                .map(item -> new Line(item.menuId(), item.quantity()))
                .toList();
        return place(request.email(), request.address(), request.postcode(), request.memo(), lines);
    }

    /** A12 이메일별 주문 목록 */
    public List<OrderResponse> findByEmail(String email) {
        return orderRepository.findByEmailOrderByIdDesc(email).stream()
                .map(OrderResponse::from)
                .toList();
    }

    /** A13 주문 상세 */
    public OrderResponse findById(Long orderId) {
        return OrderResponse.from(getOrder(orderId));
    }

    /** A16 주문 취소 */
    @Transactional
    public OrderResponse cancel(Long orderId) {
        Order order = getOrder(orderId);

        // 이미 취소된 주문 → 시각과 무관하게 200 + 최초 cancelledAt 유지
        if (order.isCancelled()) {
            return OrderResponse.from(order);
        }

        LocalDateTime now = now();
        if (!shippingPolicy.isCancellable(order.getShippingDate(), now)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_CANCELLABLE);
        }
        order.cancel(now);   // 변경 감지(dirty checking)로 커밋 시 UPDATE
        return OrderResponse.from(order);
    }

    /** A22 주문 메모 수정 */
    @Transactional
    public OrderResponse updateMemo(Long orderId, MemoUpdateRequest request) {
        Order order = getOrder(orderId);
        order.changeMemo(request.memo());
        return OrderResponse.from(order);
    }

    /**
     * A23 재주문.
     * 원본의 이메일·주소·우편번호·메뉴 ID·수량을 가져오고,
     * 메뉴명·단가는 현재 DB 값으로 새로 계산한다. (판매 중지 메뉴면 409)
     * 메모 복사 여부는 명세서 확인 후 조정.
     */
    @Transactional
    public OrderResponse reorder(Long orderId) {
        Order origin = getOrder(orderId);
        List<Line> lines = origin.getItems().stream()
                .map(item -> new Line(item.getMenuId(), item.getQuantity()))
                .toList();
        return place(origin.getEmail(), origin.getAddress(), origin.getPostcode(), null, lines);
    }

    // ===== 공통: 404 → 409 순서로 검증 후 저장 =====
    private OrderResponse place(String email, String address, String postcode,
                                String memo, List<Line> lines) {
        // 1) 404 — 모든 메뉴 존재 확인
        List<Menu> menus = lines.stream()
                .map(line -> menuRepository.findById(line.menuId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND)))
                .toList();

        // 2) 409 — 판매 중인지 확인
        if (menus.stream().anyMatch(menu -> !menu.isAvailable())) {
            throw new BusinessException(ErrorCode.MENU_UNAVAILABLE);
        }

        // 3) 409 — 같은 이메일·배송일의 기존 접수 주문과 배송지 일치 확인
        LocalDateTime now = now();
        LocalDate shippingDate = shippingPolicy.shippingDateOf(now);
        orderRepository.findFirstByEmailAndShippingDateAndStatus(email, shippingDate, OrderStatus.PLACED)
                .filter(previous -> !previous.hasSameDestination(address, postcode))
                .ifPresent(previous -> {
                    throw new BusinessException(ErrorCode.ADDRESS_MISMATCH);
                });

        // 4) 저장 — 하나의 트랜잭션: 중간 실패 시 주문·항목 모두 롤백
        Order order = Order.create(email, address, postcode, memo, now, shippingDate);
        for (int i = 0; i < lines.size(); i++) {
            order.addItem(OrderItem.of(menus.get(i), lines.get(i).quantity()));
        }
        return OrderResponse.from(orderRepository.save(order));
    }

    /**
     * 현재 서울 시각(초 단위로 절사).
     * MySQL DATETIME은 소수점 초를 반올림하므로, 13:59:59.7이 14:00:00으로 저장되는 것을 막는다.
     * 배송일 계산과 저장값이 항상 같은 시각을 기준으로 하게 된다.
     */
    private LocalDateTime now() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private record Line(Long menuId, int quantity) {
    }
}
