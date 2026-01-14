package com.example.user.exception;

import com.example.user.dto.response.ApiResponseEntity;
import com.example.user.dto.response.ApiResponseFactory;
import com.example.user.utils.LogMaskingUtil;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Global Exception Handler - Bộ xử lý lỗi tập trung toàn ứng dụng.
 * <p>
 * Class này có nhiệm vụ bắt (catch) các ngoại lệ (Exception) ném ra từ Controller, Service,
 * Repository và chuyển đổi chúng thành định dạng phản hồi chuẩn {@link ApiResponseEntity}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String JSON_PARSE_ERROR = "Malformed request payload. Please check your JSON format.";
    private static final String DATE_FORMAT_ERROR = "Invalid date format. Please use the standard format: yyyy-MM-dd.";
    private static final String DATETIME_FORMAT_ERROR = "Invalid date time format. Please use the standard format: yyyy-MM-ddTHH:mm:ss.nanoseconds.";
    private static final String ENUM_ERROR_FORMAT = "Invalid value '%s'. Allowed values: %s";
    private static final String VALIDATE_FAILED = "Validation failed";

    /**
     * Xử lý ngoại lệ {@link BadRequestException}.
     * <p>Được gọi khi dữ liệu đầu vào vi phạm quy tắc nghiệp vụ (ví dụ: Số dư không đủ).</p>
     *
     * @param ex      Đối tượng ngoại lệ chứa thông báo lỗi chi tiết.
     * @param request Đối tượng request hiện tại, dùng để ghi log URI xảy ra lỗi.
     * @return {@link ApiResponseEntity} chứa mã lỗi 400 và message từ exception.
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<?> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.warn("BadRequest at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return ApiResponseFactory.badRequest(ex.getMessage());
    }

    /**
     * Xử lý ngoại lệ {@link ConflictException}.
     * <p>Thường dùng khi vi phạm ràng buộc unique (trùng email, username, mã giao dịch...).</p>
     *
     * @param ex      Đối tượng ngoại lệ chứa thông tin xung đột.
     * @param request Đối tượng request hiện tại.
     * @return {@link ApiResponseEntity} chứa mã lỗi 409 (Conflict).
     */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponseEntity<?> handleConflict(ConflictException ex, HttpServletRequest request) {
        log.warn("Conflict at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return ApiResponseFactory.conflict(ex.getMessage());
    }

    /**
     * Xử lý ngoại lệ {@link NotFoundException}.
     * <p>Được gọi khi không tìm thấy tài nguyên yêu cầu trong cơ sở dữ liệu.</p>
     *
     * @param ex      Đối tượng ngoại lệ chứa thông báo resource không tồn tại.
     * @param request Đối tượng request hiện tại.
     * @return {@link ApiResponseEntity} chứa mã lỗi 404 (Not Found).
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponseEntity<?> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        log.warn("NotFound at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return ApiResponseFactory.notFound(ex.getMessage());
    }

    /**
     * Xử lý ngoại lệ {@link IllegalArgumentException}.
     * <p>Bắt các lỗi tham số không hợp lệ do Java Core ném ra (thường dùng trong các hàm tiện ích).</p>
     *
     * @param ex      Đối tượng ngoại lệ.
     * @param request Đối tượng request hiện tại.
     * @return {@link ApiResponseEntity} chứa mã lỗi 400 (Bad Request).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("IllegalArgument at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return ApiResponseFactory.badRequest(ex.getMessage());
    }

    /**
     * Xử lý lỗi validate DTO (khi dùng annotation @Valid @RequestBody).
     *
     * @param ex Ngoại lệ chứa danh sách các field bị lỗi validation.
     * @return {@link ApiResponseEntity} chứa mã lỗi 400 và Map danh sách lỗi (key: field, value: message).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ApiResponseFactory.badRequest(VALIDATE_FAILED, errors);
    }

    /**
     * Xử lý lỗi validate tham số đơn lẻ (@RequestParam, @PathVariable) hoặc Entity.
     *
     * @param ex Ngoại lệ chứa danh sách các vi phạm ràng buộc (Constraint Violation).
     * @return {@link ApiResponseEntity} chứa mã lỗi 400 và Map danh sách lỗi.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }
        return ApiResponseFactory.badRequest(VALIDATE_FAILED, errors);
    }

    /**
     * Xử lý lỗi sai định dạng JSON (Jackson Parse Error).
     * <p>Bóc tách lỗi cụ thể cho Date, DateTime và Enum để thông báo dễ hiểu hơn.</p>
     *
     * @param ex      Ngoại lệ Jackson khi parse JSON thất bại.
     * @param request Đối tượng request hiện tại.
     * @return {@link ApiResponseEntity} chứa mã lỗi 400 và hướng dẫn sửa format đúng.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<?> handleJsonErrors(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = JSON_PARSE_ERROR;

        if (ex.getCause() instanceof InvalidFormatException ife) {
            if (ife.getTargetType().equals(LocalDate.class)) {
                message = DATE_FORMAT_ERROR;
            } else if (ife.getTargetType().equals(LocalDateTime.class)) {
                message = DATETIME_FORMAT_ERROR;
            } else if (ife.getTargetType().isEnum()) {
                message = String.format(ENUM_ERROR_FORMAT,
                        ife.getValue(),
                        Arrays.toString(ife.getTargetType().getEnumConstants()));
            }
        }
        log.warn("JSON Parse Error at [{}]: {}", request.getRequestURI(), message);
        return ApiResponseFactory.badRequest(message);
    }

    /**
     * Xử lý lỗi sai kiểu dữ liệu trên URL.
     * <p>Ví dụ: API yêu cầu ID là Long nhưng client truyền String.</p>
     *
     * @param ex Ngoại lệ chứa tên tham số và kiểu dữ liệu mong đợi.
     * @return {@link ApiResponseEntity} chứa mã lỗi 400 và thông báo kiểu dữ liệu đúng.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid parameter '%s'. Expected type: '%s'",
                ex.getName(), Objects.requireNonNull(ex.getRequiredType()).getSimpleName());
        return ApiResponseFactory.badRequest(message);
    }

    /**
     * Xử lý lỗi parse thời gian không đúng format (Java Time API).
     *
     * @param ex Ngoại lệ parse date time.
     * @return {@link ApiResponseEntity} chứa mã lỗi 400.
     */
    @ExceptionHandler(DateTimeParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<?> handleDateTimeParse(DateTimeParseException ex) {
        return ApiResponseFactory.badRequest(DATETIME_FORMAT_ERROR);
    }

    /**
     * Xử lý lỗi format số (Ví dụ: parse "abc" thành Integer).
     *
     * @param ex Ngoại lệ format số.
     * @return {@link ApiResponseEntity} chứa mã lỗi 400.
     */
    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<?> handleNumberFormat(NumberFormatException ex) {
        return ApiResponseFactory.badRequest("Numeric format invalid.");
    }

    /**
     * Xử lý lỗi chưa đăng nhập hoặc token không hợp lệ.
     *
     * @param ex      Ngoại lệ Unauthorized.
     * @param request Đối tượng request hiện tại.
     * @return {@link ApiResponseEntity} chứa mã lỗi 401 (Unauthorized).
     */
    @ExceptionHandler(UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponseEntity<?> handleUnAuthorized(UnAuthorizedException ex, HttpServletRequest request) {
        log.warn("Unauthorized Access at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return ApiResponseFactory.unauthorized(ex.getMessage());
    }

    /**
     * Xử lý lỗi không có quyền truy cập (Role không đủ).
     *
     * @param ex      Ngoại lệ Forbidden.
     * @param request Đối tượng request hiện tại.
     * @return {@link ApiResponseEntity} chứa mã lỗi 403 (Forbidden).
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponseEntity<?> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        log.warn("Forbidden Access at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return ApiResponseFactory.forbidden(ex.getMessage());
    }

    /**
     * Xử lý lỗi trong quá trình xử lý dữ liệu bảo mật (Decrypt, Verify Signature...).
     *
     * @param ex      Ngoại lệ xử lý bảo mật.
     * @param request Đối tượng request hiện tại.
     * @return {@link ApiResponseEntity} chứa mã lỗi 400 (Bad Request).
     */
    @ExceptionHandler(SecurityProcessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<?> handleSecurityProcess(SecurityProcessException ex, HttpServletRequest request) {
        // Logback sẽ tự động Masking message này nhờ cấu hình xml
        log.warn("SECURITY INCIDENT at [{}]: {}", request.getRequestURI(), ex.getMessage());
        return ApiResponseFactory.badRequest("Security violation: Unable to process secure data.");
    }

    /**
     * Xử lý lỗi cấu hình bảo mật hệ thống (mất file key, thuật toán không hỗ trợ).
     *
     * @param ex Ngoại lệ cấu hình bảo mật.
     * @return {@link ApiResponseEntity} chứa mã lỗi 500 (Internal Server Error).
     */
    @ExceptionHandler(SecurityConfigException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseEntity<?> handleSecurityConfig(SecurityConfigException ex) {
        log.error("CRITICAL SECURITY CONFIG ERROR", ex);
        return ApiResponseFactory.internalError();
    }


    /**
     * Xử lý tất cả các lỗi còn lại chưa được bắt (Unhandled Exception).
     *
     * @param ex      Ngoại lệ không xác định (gốc).
     * @param request Đối tượng request hiện tại.
     * @return {@link ApiResponseEntity} chứa mã lỗi 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseEntity<?> handleException(Exception ex, HttpServletRequest request) {
        log.error("UNHANDLED EXCEPTION at [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ApiResponseFactory.internalError();
    }
}
