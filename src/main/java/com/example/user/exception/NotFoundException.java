package com.example.user.exception;

/**
 * Đại diện cho lỗi 404 Not Found.
 * <p>
 * Sử dụng khi: Không tìm thấy tài nguyên yêu cầu trong Database.
 * Ví dụ: Tìm user theo ID không tồn tại, Xóa sản phẩm đã bị xóa trước đó.
 */
public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(message);
    }
    public NotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
