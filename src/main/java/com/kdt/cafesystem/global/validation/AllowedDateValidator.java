package com.kdt.cafesystem.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class AllowedDateValidator implements ConstraintValidator<AllowedDate, LocalDate> {

    private static final LocalDate MIN = LocalDate.of(2000, 1, 1);
    private static final LocalDate MAX = LocalDate.of(2099, 12, 31);

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;  // 값이 없는 경우는 @RequestParam이 따로 400 처리
        }
        return !value.isBefore(MIN) && !value.isAfter(MAX);
    }
}
