package com.example.user.dto.response;


import com.example.user.constant.ApiLabelConstants;

/**
 * Factory class hỗ trợ tạo nhanh các đối tượng {@link ApiResponseEntity}.
 */
public class ApiResponseFactory {
    private static final String UNHANDLED_EXCEPTION = "An unexpected system error occurred. Please contact the administrator.";

    private ApiResponseFactory(){}

    /**
     * Trả về response thành công (HTTP 200) với dữ liệu.
     *
     * @param data Dữ liệu trả về (User, List, Object...).
     * @param <T>  Kiểu dữ liệu của data.
     * @return ApiResponseEntity chứa data và label SUCCESS.
     */
    public static <T> ApiResponseEntity<T> success(T data) {
        return ApiResponseEntity.<T>builder()
                .label(ApiLabelConstants.SUCCESS)
                .data(data)
                .build();
    }

    /**
     * Trả về response thành công có phân trang (HTTP 200).
     *
     * @param data     Danh sách dữ liệu của trang hiện tại.
     * @param pageInfo Thông tin meta về phân trang (tổng số trang, trang hiện tại...).
     * @param <T>      Kiểu dữ liệu của data.
     * @return ApiResponseEntity chứa data, pageInfo và label SUCCESS.
     */
    public static <T> ApiResponseEntity<T> success(T data, ApiResponseEntity.PageInfo pageInfo) {
        return ApiResponseEntity.<T>builder()
                .label(ApiLabelConstants.SUCCESS)
                .data(data)
                .page(pageInfo)
                .build();
    }

    /**
     * Trả về response khi tạo mới thành công (HTTP 201).
     *
     * @param data Dữ liệu vừa được tạo.
     * @param <T>  Kiểu dữ liệu.
     * @return ApiResponseEntity với label CREATED.
     */
    public static <T> ApiResponseEntity<T> created(T data) {
        return ApiResponseEntity.<T>builder()
                .label(ApiLabelConstants.CREATED)
                .data(data)
                .build();
    }

    /**
     * Trả về response khi cập nhật thành công (HTTP 200).
     *
     * @param data Dữ liệu sau khi cập nhật.
     * @param <T>  Kiểu dữ liệu.
     * @return ApiResponseEntity với label UPDATED.
     */
    public static <T> ApiResponseEntity<T> updated(T data) {
        return ApiResponseEntity.<T>builder()
                .label(ApiLabelConstants.UPDATED)
                .data(data)
                .build();
    }

    /**
     * Trả về response khi xóa thành công (HTTP 200/204).
     * <p>
     * Không trả về data để tiết kiệm băng thông.
     *
     * @return ApiResponseEntity với label DELETED và data null.
     */
    public static ApiResponseEntity<Void> deleted() {
        return ApiResponseEntity.<Void>builder()
                .label(ApiLabelConstants.DELETED)
                .build();
    }

    /**
     * Trả về lỗi Bad Request (HTTP 400).
     * Dùng khi validate dữ liệu thất bại hoặc lỗi nghiệp vụ.
     *
     * @param message Chi tiết lỗi.
     */
    public static ApiResponseEntity<Void> badRequest(String message) {
        return ApiResponseEntity.<Void>builder()
                .label(ApiLabelConstants.BAD_REQUEST)
                .message(message)
                .build();
    }

    /**
     * Trả về phản hồi lỗi Bad Request (HTTP 400).
     * Thường dùng khi dữ liệu đầu vào không hợp lệ (Validation errors) để trả về chi tiết lỗi cho từng trường.
     *
     * @param message Thông báo tóm tắt về lỗi.
     * @param data    Dữ liệu chi tiết lỗi (thường là Map hoặc List các trường bị lỗi).
     * @param <T>     Kiểu dữ liệu của đối tượng lỗi.
     * @return Đối tượng ApiResponseEntity chứa thông tin lỗi 400.
     */
    public static <T> ApiResponseEntity<T> badRequest(String message, T data) {
        return ApiResponseEntity.<T>builder()
                .label(ApiLabelConstants.BAD_REQUEST)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Trả về lỗi Not Found (HTTP 404).
     * Dùng khi không tìm thấy tài nguyên trong DB.
     *
     * @param message Chi tiết lỗi (ví dụ: "User not found").
     */
    public static ApiResponseEntity<Void> notFound(String message) {
        return ApiResponseEntity.<Void>builder()
                .label(ApiLabelConstants.NOT_FOUND)
                .message(message)
                .build();
    }

    /**
     * Trả về lỗi Conflict (HTTP 409).
     * Dùng khi vi phạm ràng buộc dữ liệu (ví dụ: Trùng email).
     *
     * @param message Chi tiết lỗi.
     */
    public static ApiResponseEntity<Void> conflict(String message) {
        return ApiResponseEntity.<Void>builder()
                .label(ApiLabelConstants.CONFLICT)
                .message(message)
                .build();
    }

    public static ApiResponseEntity<Void> unauthorized(String message) {
        return ApiResponseEntity.<Void>builder()
                .label(ApiLabelConstants.UNAUTHORIZED)
                .message(message)
                .build();
    }

    public static ApiResponseEntity<Void> forbidden(String message) {
        return ApiResponseEntity.<Void>builder()
                .label(ApiLabelConstants.FORBIDDEN)
                .message(message)
                .build();
    }

    /**
     * Dùng cho các lỗi về mã hóa.
     * Sử dụng message mặc định từ ErrorMessages để bảo mật thông tin hệ thống.
     * @param message Chi tiết lỗi.
     */
    public static ApiResponseEntity<Void> cryptoError(String message) {
        return ApiResponseEntity.<Void>builder()
                .label(ApiLabelConstants.CRYPTO_ERROR)
                .message(message)
                .build();
    }

    /**
     * Trả về lỗi Internal Server Error (HTTP 500).
     * Dùng cho các lỗi hệ thống không mong muốn (Exception handler).
     * Sử dụng message mặc định từ ErrorMessages để bảo mật thông tin hệ thống.
     */
    public static ApiResponseEntity<Void> internalError() {
        return ApiResponseEntity.<Void>builder()
                .label(ApiLabelConstants.INTERNAL_ERROR)
                .message(UNHANDLED_EXCEPTION)
                .build();
    }
}
