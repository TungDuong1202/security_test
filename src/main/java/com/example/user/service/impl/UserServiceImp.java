package com.example.user.service.impl;

import com.example.user.dto.request.CreateUserRequest;
import com.example.user.dto.request.UpdateUserRequest;
import com.example.user.dto.response.UserResponse;
import com.example.user.entity.User;
import com.example.user.entity.UserProfile;
import com.example.user.enums.Gender;
import com.example.user.enums.Role;
import com.example.user.enums.UserStatus;
import com.example.user.exception.BadRequestException;
import com.example.user.exception.NotFoundException;
import com.example.user.repository.IUserRepository;
import com.example.user.service.IUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements IUserService {
    private final IUserRepository IUserRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String USER_NOT_FOUND = "User not found";
    private static final String EMAIL_EXIST = "Email already exists";
    public static final String USER_ID_REQUIRED = "User ID is required";

    /**
     * Lấy danh sách người dùng có phân trang.
     * Chỉ lấy những user có trạng thái ACTIVE.
     *
     * @param pageable Đối tượng chứa thông tin phân trang (page, size, sort).
     * @return Page<UserResponse> Trang chứa danh sách user đã map sang DTO.
     */
    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        if (Objects.isNull(pageable)) {
            pageable = Pageable.unpaged();
        }
        return IUserRepository.findAllUserByStatus(UserStatus.ACTIVE, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Tạo người dùng mới
     *
     * @param request DTO chứa thông tin đăng ký.
     * @return UserResponse Thông tin user vừa tạo.
     * @throws BadRequestException Nếu email đã tồn tại.
     */
    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Tìm xem email đã có trong DB chưa
        Optional<User> existingUserOpt = IUserRepository.findByEmail(request.getEmail());
        User user;
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.getStatus().equals(UserStatus.DELETED)) {
                user = existingUser;
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setFullName(request.getFullName());

                // Cập nhật thông tin profile mới đè lên cái cũ
                updateUserProfile(user, request.getPhone(), request.getAddress(), request.getBirthday(), request.getGender());
            } else throw new BadRequestException(EMAIL_EXIST);

        } else {
            // Email chưa từng tồn tại -> Tạo mới hoàn toàn
            user = User.builder()
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .role(Role.USER)
                    .status(UserStatus.ACTIVE)
                    .build();

            UserProfile profile = UserProfile.builder()
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .birthday(request.getBirthday())
                    .gender(request.getGender())
                    .build();
            user.setProfile(profile);
        }
        return mapToResponse(IUserRepository.save(user));
    }

    /**
     * Cập nhật thông tin người dùng.
     *
     * @param userId ID của người dùng cần sửa.
     * @param request DTO chứa thông tin cần sửa.
     * @return UserResponse Thông tin user sau khi cập nhật.
     * @throws NotFoundException Nếu không tìm thấy user.
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = IUserRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        updateUserProfile(user, request.getPhone(), request.getAddress(), request.getBirthday(), request.getGender());
        return mapToResponse(IUserRepository.save(user));
    }

    /**
     * Xóa người dùng (Xóa mềm - Soft Delete).
     * <p>
     * Chuyển trạng thái user sang DELETED chứ không xóa hẳn khỏi DB.
     *
     * @param userId ID của user cần xóa.
     * @throws NotFoundException Nếu user không tồn tại.
     */
    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = IUserRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        user.setStatus(UserStatus.DELETED);
    }

    @Override
    public void updateUserProfile(User user, String phone, String address, LocalDate birthday, Gender gender) {
        if (user == null) return;
        UserProfile profile = user.getProfile();
        if (Objects.isNull(profile)) {
            profile = new UserProfile();
            user.setProfile(profile);
        }

        profile.setPhone(phone);
        profile.setAddress(address);
        profile.setBirthday(birthday);
        profile.setGender(gender);
    }

    private UserResponse mapToResponse(User user) {
        if (Objects.isNull(user)) return null;

        UserProfile profile = Objects.nonNull(user.getProfile()) ? user.getProfile() : new UserProfile();

        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .fullName(user.getFullName())
                .createdAt(user.getCreatedAt())
                .phone(profile.getPhone())
                .address(profile.getAddress())
                .birthday(profile.getBirthday())
                .gender(profile.getGender())
                .build();
    }
}
