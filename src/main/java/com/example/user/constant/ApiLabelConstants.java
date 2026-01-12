package com.example.user.constant;

/**
 * Lớp chứa các hằng số thông báo (Label) chuẩn cho API.
 * Mục đích:
 * 1. Tránh hardcode chuỗi ký tự ở nhiều nơi
 * 2. Đảm bảo tính nhất quán của thông báo trả về trên toàn hệ thống
 * 3. Dễ dàng sửa đổi nội dung thông báo tại một nơi duy nhất
 */
public final class ApiLabelConstants {
    private ApiLabelConstants(){}

    /** Thông báo mặc định cho thành công (HTTP 200). */
    public static final String SUCCESS = "Success";

    /** Thông báo khi tạo mới tài nguyên thành công (HTTP 201). */
    public static final String CREATED = "Created successfully";

    /** Thông báo khi cập nhật tài nguyên thành công. */
    public static final String UPDATED = "Updated successfully";

    /** Thông báo khi xóa tài nguyên thành công. */
    public static final String DELETED = "Deleted successfully";

    /** Thông báo lỗi dữ liệu gửi lên không hợp lệ (HTTP 400). */
    public static final String BAD_REQUEST = "Bad request";

    /** Thông báo lỗi không tìm thấy tài nguyên (HTTP 404). */
    public static final String NOT_FOUND = "Not found";

    /** Thông báo lỗi xung đột dữ liệu (HTTP 409). */
    public static final String CONFLICT = "Conflict";

    /** Thông báo lỗi chưa đăng nhập (HTTP 401). */
    public static final String UNAUTHORIZED = "Unauthorized";

    /** Thông báo lỗi không có quyền (HTTP 403). */
    public static final String FORBIDDEN = "Forbidden";

    /** Thông báo lỗi mã hóa. */
    public static final String CRYPTO_ERROR = "Crypto error";

    /** Thông báo lỗi hệ thống không mong muốn (HTTP 500). */
    public static final String INTERNAL_ERROR = "Internal server error";
}
