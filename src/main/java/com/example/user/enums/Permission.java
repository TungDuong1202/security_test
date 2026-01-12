package com.example.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {
    USER_READ("/api/users/**", "GET"),
    USER_CREATE("/api/users", "POST"),
    USER_UPDATE("/api/users/**", "PUT"),
    USER_DELETE("/api/users/**", "DELETE");


    private final String url;
    private final String method;
}
