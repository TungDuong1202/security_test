package com.example.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lớp bao bọc (Wrapper) chuẩn cho mọi phản hồi API của hệ thống.
 * <p>
 * Ví dụ JSON:
 * <pre>
 * {
 * "label": "SUCCESS",
 * "data": { ... },
 * "page": { ... } // Chỉ hiện khi có phân trang
 * }
 * </pre>
 *
 * @param <T> Kiểu dữ liệu của payload (UserResponse, List<UserResponse>, etc.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseEntity<T>{
    private String label;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;
    private T data;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PageInfo page;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageInfo {
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}
