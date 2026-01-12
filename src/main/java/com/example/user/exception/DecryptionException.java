package com.example.user.exception;

public class DecryptionException extends BaseException {
    public DecryptionException(String message) {
        super(message);
    }
    public DecryptionException(String message, Throwable cause){
        super(message, cause);
    }
}
