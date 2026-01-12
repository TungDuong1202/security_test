package com.example.user.entity;
import com.example.user.enums.Role;
import com.example.user.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO nhận dữ liệu từ Client để tạo mới User.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// validate
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @Setter(AccessLevel.NONE)
    private Long userId;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "{user.email.required}")
    @Email(message = "{user.email.invalid}")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "{user.password.required}")
    private String password;

    @Column(name = "full_name", nullable = false)
    @NotBlank(message = "{user.fullName.required}")
    private String fullName;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserProfile profile;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Thiết lập hồ sơ (Profile) cho người dùng và đồng bộ hóa quan hệ hai chiều.
     * <p>
     * Phương thức này không chỉ gán profile cho user, mà còn tự động gán ngược lại
     * tham chiếu {@code User} vào trong đối tượng {@code UserProfile}.
     * Điều này đảm bảo tính nhất quán dữ liệu (Data Consistency) trước khi lưu xuống database.
     *
     * @param profile Đối tượng {@link UserProfile} cần gán cho người dùng này.
     */
    public void setProfile(UserProfile profile) {
        this.profile = profile;
        if (Objects.nonNull(profile)) {
            profile.setUser(this);
        }
    }
}
