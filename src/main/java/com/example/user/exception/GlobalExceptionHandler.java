package com.example.user.exception;

import com.example.user.dto.response.ApiResponseEntity;
import com.example.user.dto.response.ApiResponseFactory;
import com.example.user.utils.LogMaskingUtil;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
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
    private static final String ENUM_ERROR_FORMAT = "Invalid value '%s'. Allowed values: %s";
    private static final String VALIDATE_FAILED = "Validation failed";

    /**
     * Xử lý ngoại lệ {@link BadRequestException}.
     * <p>
     * Được gọi khi có lỗi logic nghiệp vụ do client gửi dữ liệu không hợp lệ
     * (mà không phải do Validate Annotation).
     *
     * @param ex Ngoại lệ chứa message lỗi cụ thể.
     * @return Response lỗi 400 Bad Request.
     */
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<?> handleBadRequest(BadRequestException ex) {
        return ApiResponseFactory.badRequest(ex.getMessage());
    }
    /**
     * Xử lý ngoại lệ {@link ConflictException}.
     * <p>
     * Thường được gọi khi vi phạm ràng buộc dữ liệu, ví dụ: Trùng email, trùng số điện thoại.
     *
     * @param ex Ngoại lệ chứa thông tin xung đột.
     * @return Response lỗi 409 Conflict.
     */
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponseEntity<?> handleConflict(ConflictException ex) {
        return ApiResponseFactory.conflict(ex.getMessage());
    }
    /**
     * Xử lý ngoại lệ {@link NotFoundException}.
     * <p>
     * Được gọi khi không tìm thấy tài nguyên yêu cầu (User không tồn tại, ID sai...).
     *
     * @param ex Ngoại lệ chứa thông báo không tìm thấy.
     * @return Response lỗi 404 Not Found.
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponseEntity<?> handleNotFound(NotFoundException ex) {
        return ApiResponseFactory.notFound(ex.getMessage());
    }
    /**
     * Xử lý ngoại lệ {@link IllegalArgumentException}.
     * <p>
     * Bắt các lỗi tham số truyền vào phương thức không hợp lệ (thường do Java Core ném ra).
     *
     * @param ex Ngoại lệ tham số không hợp lệ.
     * @return Response lỗi 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponseFactory.badRequest(ex.getMessage());
    }
    /**
     * Xử lý ngoại lệ Validate thủ công (hoặc @RequestParam/@PathVariable).
     * <p>
     * Đồng bộ format trả về giống hệt MethodArgumentNotValidException:
     * Trả về Map<String, String> với key là tên field, value là message lỗi.
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
     * Xử lý ngoại lệ Validate DTO body (@Valid @RequestBody).
     * <p>
     * Tổng hợp tất cả các lỗi validate trong DTO (ví dụ: Email rỗng, password ngắn)
     *
     * @param ex Ngoại lệ chứa danh sách các field bị lỗi.
     * @return Response lỗi 400 Bad Request kèm danh sách lỗi chi tiết.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ApiResponseFactory.badRequest(VALIDATE_FAILED, errors);
    }
    /**
     * Xử lý lỗi định dạng JSON (JSON Parse Error).
     * <p>
     * Bắt các lỗi khi Jackson không thể map JSON từ request vào Object Java.
     * Đặc biệt xử lý riêng cho trường hợp sai định dạng ngày tháng (LocalDate).
     *
     * @param ex Ngoại lệ đọc dữ liệu HTTP.
     * @return Response lỗi 400 Bad Request với thông báo thân thiện.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<?> handleJsonErrors(HttpMessageNotReadableException ex) {
        String message = JSON_PARSE_ERROR;

        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) ex.getCause();
            // Kiểm tra xem lỗi có phải do trường LocalDate không
            if (ife.getTargetType().equals(LocalDate.class)) {
                message = DATE_FORMAT_ERROR;
            }
            // Trường hợp 2: Lỗi định dạng Enum (Gender, Role, UserStatus...)
            else if (ife.getTargetType().isEnum()) {
                message = String.format(ENUM_ERROR_FORMAT,
                        ife.getValue(),
                        Arrays.toString(ife.getTargetType().getEnumConstants()));
            }
        }

        return ApiResponseFactory.badRequest(message);
    }

    /**
     *  Xử lý lỗi Bảo Mật Dữ Liệu (400).
     * <p>
     * Bắt lỗi: Decrypt thất bại, Sai chữ ký RSA, Input Base64 lỗi.
     * Đây thường là dấu hiệu của việc tấn công hoặc dữ liệu sai format.
     */
    @ExceptionHandler(SecurityProcessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<?> handleSecurityProcess(SecurityProcessException ex) {
        String safeMsg = LogMaskingUtil.mask(ex.getMessage());
        log.warn("SECURITY INCIDENT: {}", safeMsg);
        return ApiResponseFactory.badRequest("Security violation: Unable to process secure data.");
    }

    /**
     *  Xử lý lỗi Cấu Hình Bảo Mật (500).
     * <p>
     * Bắt lỗi: Server thiếu thuật toán, File Keystore bị hỏng.
     */
    @ExceptionHandler(SecurityConfigException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseEntity<?> handleSecurityConfig(SecurityConfigException ex) {
        log.error("SECURITY CONFIG ERROR: Encryption setup failed.", ex);
        return ApiResponseFactory.internalError();
    }

    /**
     * [MỚI] Xử lý lỗi sai kiểu dữ liệu trên URL.
     * <p>
     * Ví dụ: /api/users/{id} yêu cầu Long, nhưng truyền /api/users/abc
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid parameter '%s'. Expected type: '%s'",
                ex.getName(), Objects.requireNonNull(ex.getRequiredType()).getSimpleName());
        return ApiResponseFactory.badRequest(message);
    }


    @ExceptionHandler(UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponseEntity<?> handleUnAuthorized(UnAuthorizedException ex){
        return ApiResponseFactory.unauthorized(ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponseEntity<?> handleForbidden(ForbiddenException ex){
        return ApiResponseFactory.forbidden(ex.getMessage());
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponseEntity<?> handleException(Exception ex) {
        String safeMessage = LogMaskingUtil.mask(ex.getMessage());
        log.error("UNHANDLED EXCEPTION: {}", safeMessage, ex);
        return ApiResponseFactory.internalError();
    }
}
