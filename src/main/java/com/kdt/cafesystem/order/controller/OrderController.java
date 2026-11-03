package com.kdt.cafesystem.order.controller;

import com.kdt.cafesystem.order.dto.MemoUpdateRequest;
import com.kdt.cafesystem.order.dto.OrderCreateRequest;
import com.kdt.cafesystem.order.dto.OrderResponse;
import com.kdt.cafesystem.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 주문 API. ※ 경로·HTTP 메서드·상태 코드는 명세서와 반드시 대조할 것.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$";

    private final OrderService orderService;

    /** A11 주문 접수 */
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = orderService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + response.id())).body(response);
    }

    /** A12 이메일별 주문 목록  GET /api/v1/orders?email=... */
    @GetMapping
    public List<OrderResponse> findByEmail(
            @RequestParam
            @Size(min = 1, max = 100, message = "이메일은 1~100자입니다.")
            @Pattern(regexp = EMAIL_REGEX, message = "이메일 형식이 올바르지 않습니다.")
            String email) {
        return orderService.findByEmail(email);
    }

    /** A13 주문 상세 */
    @GetMapping("/{orderId}")
    public OrderResponse findById(@PathVariable Long orderId) {
        return orderService.findById(orderId);
    }

    /** A16 주문 취소 */
    @PostMapping("/{orderId}/cancel")
    public OrderResponse cancel(@PathVariable Long orderId) {
        return orderService.cancel(orderId);
    }

    /** A22 주문 메모 수정 */
    @PutMapping("/{orderId}/memo")
    public OrderResponse updateMemo(@PathVariable Long orderId,
                                    @Valid @RequestBody MemoUpdateRequest request) {
        return orderService.updateMemo(orderId, request);
    }

    /** A23 재주문 */
    @PostMapping("/{orderId}/reorder")
    public ResponseEntity<OrderResponse> reorder(@PathVariable Long orderId) {
        OrderResponse response = orderService.reorder(orderId);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + response.id())).body(response);
    }
}
