package com.example.user.exception;

public class UnAuthorizedException extends BaseException {
    public UnAuthorizedException(String message) {
        super(message);
    }
    public UnAuthorizedException(String message, Throwable cause){
        super(message, cause);
    }
}
