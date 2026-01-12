package com.example.user.service;

import com.example.user.dto.request.CreateUserRequest;
import com.example.user.dto.request.LoginRequest;
import com.example.user.dto.response.AuthResponse;
import com.example.user.dto.response.UserResponse;
import com.example.user.exception.BadRequestException;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến xác thực và định danh người dùng.
 */
public interface IAuthService {
    /**
     * Đăng ký người dùng mới vào hệ thống.
     *
     * @param request DTO chứa thông tin đăng ký (Email, Password, Họ tên, ...).
     * @return DTO chứa thông tin người dùng sau khi tạo thành công (không bao gồm mật khẩu).
     */
    UserResponse register(CreateUserRequest request);
    /**
     * Xác thực người dùng (Đăng nhập).
     * <p>
     * Phương thức này sẽ kiểm tra thông tin đăng nhập và trả về Token nếu hợp lệ.
     *
     * @param request DTO chứa thông tin đăng nhập (Email/Username và Password).
     * @return DTO chứa thông tin định danh và Access Token (JWT).
     * @throws BadRequestException Nếu sai email, sai mật khẩu hoặc tài khoản bị xóa.
     */
    AuthResponse login(LoginRequest request);
}
