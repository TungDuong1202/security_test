package com.example.user.exception;

/**
 * Exception cơ sở cho toàn bộ ứng dụng.
 * <p>
 * Tất cả các custom exception (lỗi nghiệp vụ) đều phải kế thừa từ class này.
 * Giúp phân biệt lỗi do logic của mình tạo ra (Checked/Unchecked logic)
 * với lỗi hệ thống (NullPointer, SQLException...).
 */
public class BaseException extends RuntimeException{
    public BaseException(String message){
        super(message);
    }
    public BaseException(String message, Throwable cause){
        super(message, cause);
    }
}
