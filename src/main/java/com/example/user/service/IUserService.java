package com.example.user.service;

import com.example.user.dto.request.CreateUserRequest;
import com.example.user.dto.request.UpdateUserRequest;
import com.example.user.dto.response.UserResponse;
import com.example.user.entity.User;
import com.example.user.enums.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến người dùng.
 * Controller chỉ cần biết input/output của các hàm này mà không cần quan tâm logic bên trong.
 */
public interface IUserService {
    /**
     * Lấy danh sách người dùng có phân trang.
     *
     * @param pageable Thông tin phân trang (số trang, kích thước trang, sắp xếp).
     * @return Page chứa danh sách UserResponse và thông tin meta (total pages, total elements).
     */
    Page<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Tạo mới một người dùng vào hệ thống.
     *
     * @param request DTO chứa thông tin đăng ký (email, password, profile...).
     * @return UserResponse thông tin người dùng vừa được tạo thành công.
     * @throws com.example.user.exception.BadRequestException Nếu email đã tồn tại.
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Cập nhật thông tin của người dùng.
     * <p>
     * Lưu ý: Hàm này chỉ cập nhật các thông tin cá nhân (Họ tên, SĐT, Địa chỉ...),
     * không bao gồm việc đổi mật khẩu hay đổi quyền (Role).
     *
     * @param userId ID của người dùng cần sửa.
     * @param request DTO chứa các thông tin cần thay đổi (các trường null sẽ bị bỏ qua).
     * @return UserResponse thông tin người dùng sau khi đã cập nhật.
     * @throws com.example.user.exception.NotFoundException Nếu không tìm thấy userId.
     */
    UserResponse updateUser(Long userId, UpdateUserRequest request);
    /**
     * Xóa người dùng khỏi hệ thống (Xóa mềm).
     * <p>
     * User sẽ được chuyển trạng thái sang DELETED chứ không bị xóa vĩnh viễn khỏi Database.
     *
     * @param userId ID của người dùng cần xóa.
     * @throws com.example.user.exception.NotFoundException Nếu không tìm thấy userId.
     */
    void deleteUser(Long userId);

    public void updateUserProfile(User user, String phone, String address, LocalDate birthday, Gender gender);
}
