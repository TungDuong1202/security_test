package com.example.user.exception;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(message);
    }
    public ForbiddenException(String message, Throwable cause){
        super(message, cause);
    }
}
