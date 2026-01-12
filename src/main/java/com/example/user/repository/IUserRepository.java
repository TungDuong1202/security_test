package com.example.user.repository;

import com.example.user.entity.User;
import com.example.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
/**
 * Repository interface quản lý các thao tác Database với bảng User.
 */
public interface IUserRepository extends JpaRepository<User, Long> {
    /**
     * Lấy danh sách user theo trạng thái có phân trang.
     * <p>
     * <b>Tối ưu hiệu năng:</b> Sử dụng {@code @EntityGraph} để Fetch (tải) luôn
     * dữ liệu bảng Profile đi kèm trong 1 câu lệnh SQL duy nhất (JOIN FETCH).
     * <p>
     * Giúp tránh lỗi kinh điển "N+1 Query" khi convert sang DTO.
     *
     * @param status   Trạng thái user cần lấy.
     * @param pageable Thông tin phân trang.
     * @return Page các user kèm theo profile.
     */
    @EntityGraph(attributePaths = {"profile"})
    Page<User> findAllUserByStatus(UserStatus status, Pageable pageable);

    /**
     * Tìm user bằng email (Bất kể trạng thái ACTIVE hay DELETED).
     *
     * @param email Email cần tìm.
     * @return Optional chứa user nếu tìm thấy.
     */
    Optional<User> findByEmail(String email);

    /**
     * Tìm user theo ID và trạng thái cụ thể.
     *
     * @param userId ID user.
     * @param status Trạng thái bắt buộc (thường là ACTIVE).
     * @return Optional chứa user.
     */
    Optional<User> findByUserIdAndStatus(Long userId, UserStatus status);
}
