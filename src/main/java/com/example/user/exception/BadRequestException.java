package com.example.user.exception;
/**
 * Đại diện cho lỗi 400 Bad Request.
 * <p>
 * Sử dụng khi: Dữ liệu đầu vào không hợp lệ, vi phạm validate logic.
 * Ví dụ: Mật khẩu quá ngắn, Email không đúng định dạng, thiếu trường bắt buộc.
 */
public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(message);
    }
    public BadRequestException(String message, Throwable cause){
        super(message, cause);
    }
}
