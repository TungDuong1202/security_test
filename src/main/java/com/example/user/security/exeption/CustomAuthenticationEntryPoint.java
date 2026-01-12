package com.example.user.security.exeption;

import com.example.user.dto.response.ApiResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý lỗi xác thực không thành công (401 Unauthorized).
 * <p>
 * Class này đóng vai trò là "Cổng gác" (Entry Point), được kích hoạt khi:
 * 1. Người dùng <b>chưa đăng nhập</b> (Anonymous) cố truy cập vào API yêu cầu bảo mật.
 * 2. Token gửi lên bị lỗi (Hết hạn, sai chữ ký, rỗng...) và bị {@code JwtAuthenticationFilter} chặn lại.
 * <p>
 * Nhiệm vụ: Trả về một JSON thông báo lỗi chuẩn thay vì trang HTML lỗi mặc định của Spring/Tomcat.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Phương thức được gọi khi xảy ra lỗi AuthenticationException.
     *
     * @param request  Request HTTP ban đầu.
     * @param response Response HTTP dùng để trả về lỗi.
     * @param ex       Ngoại lệ xác thực (chứa message lỗi như "TOKEN_EXPIRED", "TOKEN_MISSING"...).
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException ex
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        mapper.writeValue(
                response.getWriter(),
                ApiResponseFactory.unauthorized(ex.getMessage())
        );
    }
}
