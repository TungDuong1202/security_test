package com.example.user.exception;

/**
 * Đại diện cho lỗi 409 Conflict.
 * <p>
 * Sử dụng khi: Xảy ra xung đột dữ liệu với Database.
 * Ví dụ: Đăng ký email đã tồn tại, Trùng mã sản phẩm...
 */
public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message);
    }
    public ConflictException(String message, Throwable cause){
        super(message, cause);
    }
}
