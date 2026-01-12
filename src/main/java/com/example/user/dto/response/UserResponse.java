package com.example.user.dto.response;

import com.example.user.enums.Gender;
import com.example.user.enums.Role;
import com.example.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dùng để trả về thông tin User cho Client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String email;
    private String fullName;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;

    private String phone;
    private String address;
    private LocalDate birthday;
    private Gender gender;
}
