package com.kdt.cafesystem.order.dto.validation;

import com.kdt.cafesystem.order.dto.OrderItemRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * items 안의 menuId 중복 검사.
 * null 목록·null 항목·null menuId는 다른 검증(@NotEmpty, @NotNull)이 잡으므로 여기서는 통과시킨다.
 */
public class UniqueMenuIdsValidator implements ConstraintValidator<UniqueMenuIds, List<OrderItemRequest>> {

    @Override
    public boolean isValid(List<OrderItemRequest> items, ConstraintValidatorContext context) {
        if (items == null) {
            return true;
        }
        Set<Long> seen = new HashSet<>();
        return items.stream()
                .filter(Objects::nonNull)
                .map(OrderItemRequest::menuId)
                .filter(Objects::nonNull)
                .allMatch(seen::add);
    }
}
