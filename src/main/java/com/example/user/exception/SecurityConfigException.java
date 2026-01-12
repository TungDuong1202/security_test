package com.example.user.exception;

public class SecurityConfigException extends BaseException {
    public SecurityConfigException(String message) {
        super(message);
    }
    public SecurityConfigException(String message, Throwable cause){
        super(message, cause);
    }
}
