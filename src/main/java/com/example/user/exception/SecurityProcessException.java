package com.example.user.exception;

public class SecurityProcessException extends BaseException {
    public SecurityProcessException(String message) {
        super(message);
    }
    public SecurityProcessException(String message, Throwable cause){
        super(message, cause);
    }
}
