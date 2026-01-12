package com.example.user.exception;

public class EncryptionException extends BaseException {
    public EncryptionException(String message) {
        super(message);
    }
    public EncryptionException(String message, Throwable cause){
        super(message, cause);
    }
}
