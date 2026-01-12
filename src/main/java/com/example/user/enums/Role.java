package com.example.user.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Chứa giá trị của trường role
 */
@Getter
@RequiredArgsConstructor
public enum Role {

    /**
     * người dùng có những quyền cơ bản
     */
    USER(Set.of(

    )),

    // STAFF: Chỉ xem và tạo user
    STAFF(Set.of(
            Permission.USER_READ,
            Permission.USER_CREATE
    )),

    // ADMIN: Có tất cả quyền
    ADMIN(Set.of(
            Permission.USER_READ,
            Permission.USER_CREATE,
            Permission.USER_UPDATE,
            Permission.USER_DELETE
    ));
    private final Set<Permission> permissions;
}
