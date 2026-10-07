package com.cinema.user_service.util.error;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.cinema.user_service.domain.RestResponse;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(value = {
            IdInvalidException.class,
            NameInvalidException.class
    })
    public ResponseEntity<RestResponse<Object>> handleIdException(Exception ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setMessage(ex.getMessage());
        res.setError("Bad Request");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(value = org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<RestResponse<Object>> handleAuthenticationException(Exception ex) {
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value()); // Hoặc HttpStatus.UNAUTHORIZED.value() tùy bạn muốn trả về
                                                           // mã mấy
        res.setMessage("Tài khoản hoặc mật khẩu không chính xác.");
        res.setError("Bad Request");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<RestResponse<Object>> handleResponseStatusException(
            ResponseStatusException ex) {

        RestResponse<Object> res = new RestResponse<>();

        res.setStatusCode(ex.getStatusCode().value());
        res.setMessage(ex.getReason());
        res.setError(HttpStatus.valueOf(ex.getStatusCode().value()).getReasonPhrase());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(res);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> validationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        final List<FieldError> fieldErrors = result.getFieldErrors();

        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getBody().getDetail());

        // Lấy danh sách thông báo lỗi từ các annotation (@NotBlank, @Email, @Size...)
        List<String> errors = fieldErrors.stream()
                .map(f -> f.getDefaultMessage())
                .collect(Collectors.toList());

        // Nếu chỉ có 1 lỗi thì trả về String, nếu nhiều lỗi thì trả về List<String>
        res.setMessage(errors.size() > 1 ? errors : errors.get(0));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
