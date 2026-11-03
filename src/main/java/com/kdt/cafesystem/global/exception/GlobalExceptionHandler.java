package com.kdt.cafesystem.global.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** @Valid @RequestBody 검증 실패 → 400 + fieldErrors */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(fe -> fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return badRequest(fieldErrors);
    }

    /** @RequestParam / @PathVariable 에 붙인 제약 위반 → 400 */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleParamValidation(HandlerMethodValidationException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        e.getParameterValidationResults().forEach(r -> {
            String name = r.getMethodParameter().getParameterName();
            r.getResolvableErrors().forEach(err ->
                    fieldErrors.putIfAbsent(name, err.getDefaultMessage()));
        });
        return badRequest(fieldErrors);
    }

    /** JSON 문법 오류, 타입 불일치(boolean 자리에 문자열 등), 필수 쿼리 누락 → 400 */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleBadInput(Exception e) {
        return badRequest(Map.of());
    }

    /** Service에서 던진 404·409 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus()).body(ErrorResponse.of(code));
    }

    private ResponseEntity<ErrorResponse> badRequest(Map<String, String> fieldErrors) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(ErrorCode.VALIDATION_ERROR, fieldErrors));
    }
}
