package com.example.user.security.exeption;

import com.example.user.dto.response.ApiResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý lỗi từ chối truy cập (403 Forbidden).
 * <p>
 * Class này được kích hoạt khi:
 * 1. User <b>ĐÃ ĐĂNG NHẬP</b> thành công (Token hợp lệ, Authentication có trong Context).
 * 2. Nhưng User <b>KHÔNG CÓ QUYỀN</b> truy cập vào tài nguyên này.
 * (Ví dụ: User thường cố vào trang Admin, hoặc sai Role).
 * <p>
 * Phân biệt với {@code AuthenticationEntryPoint}:
 * <ul>
 * <li>{@code AccessDeniedHandler}: Xử lý 403 (Biết là ai rồi nhưng cấm vào).</li>
 * <li>{@code AuthenticationEntryPoint}: Xử lý 401 (Chưa biết là ai/Token lỗi).</li>
 * </ul>
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Phương thức xử lý chính khi gặp lỗi AccessDeniedException.
     *
     * @param request  Request HTTP ban đầu.
     * @param response Response HTTP để trả về client.
     * @param ex       Ngoại lệ bị ném ra bởi Security (thường từ AuthorizationManager).
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        mapper.writeValue(
                response.getWriter(),
                ApiResponseFactory.forbidden("Bạn không có quyền truy cập")
        );
    }
}
