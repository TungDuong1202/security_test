package com.example.user.service.impl;

import com.example.user.dto.request.CreateUserRequest;
import com.example.user.dto.request.LoginRequest;
import com.example.user.dto.response.AuthResponse;
import com.example.user.dto.response.UserResponse;
import com.example.user.entity.User;
import com.example.user.enums.UserStatus;
import com.example.user.exception.BadRequestException;
import com.example.user.repository.IUserRepository;
import com.example.user.security.JwtUtils;
import com.example.user.service.IAuthService;
import com.example.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation của Authentication Service.
 * <p>
 * Class này chịu trách nhiệm xử lý các nghiệp vụ liên quan đến bảo mật và định danh:
 * <ul>
 * <li>Đăng ký tài khoản mới (thông qua UserService).</li>
 * <li>Đăng nhập và cấp phát Access Token (JWT).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImp implements IAuthService {
    private final IUserRepository userRepository;
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * Xử lý yêu cầu đăng ký tài khoản mới.
     * <p>
     * Hàm này đóng vai trò là một "Facade" (mặt tiền), ủy quyền việc tạo user
     * cho {@link IUserService} để đảm bảo nguyên lý Single Responsibility.
     *
     * @param request Dữ liệu đăng ký từ Client (Email, Pass, Info...).
     * @return Thông tin user đã được tạo thành công.
     */
    @Override
    public UserResponse register(CreateUserRequest request) {
        return userService.createUser(request);
    }

    /**
     * Xử lý đăng nhập và cấp phát Token.
     * <p>
     * Quy trình xác thực:
     * <ol>
     * <li>Kiểm tra Email có tồn tại trong Database không.</li>
     * <li>Kiểm tra trạng thái tài khoản (Active/Inactive/Locked).</li>
     * <li>Kiểm tra mật khẩu (So sánh hash).</li>
     * <li>Nếu hợp lệ -> Sinh JWT Access Token.</li>
     * </ol>
     *
     * @param request Dữ liệu đăng nhập (Email, Password).
     * @return {@link AuthResponse} chứa thông tin user và Access Token.
     * @throws BadRequestException Nếu sai email, sai mật khẩu hoặc tài khoản bị khóa.
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không đúng"));
        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new BadRequestException("Tài khoản không tồn tại");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Email hoặc mật khẩu không đúng");
        }
        String token = jwtUtils.generateAccessToken(
                user.getUserId(),
                user.getRole().name()
        );
        return AuthResponse.builder()
                .accessToken(token)
                .build();
    }
}
