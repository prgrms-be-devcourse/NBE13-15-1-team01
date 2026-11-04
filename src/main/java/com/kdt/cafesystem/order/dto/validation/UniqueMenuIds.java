package com.kdt.cafesystem.order.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 주문 항목 목록에 같은 메뉴 ID가 두 번 이상 들어 있으면 400 VALIDATION_ERROR (fieldErrors.items).
 * 필드에 붙이므로 오류 키가 "items"로 나온다.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueMenuIdsValidator.class)
public @interface UniqueMenuIds {

    String message() default "메뉴 ID는 중복 없이 보내 주세요.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
