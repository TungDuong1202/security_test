package com.example.user.security;

import com.example.user.enums.Role;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.function.Supplier;

/**
 * Bộ quản lý phân quyền động (Dynamic Authorization Manager).
 * <p>
 * Class này thay thế cho việc cấu hình cứng các quyền truy cập trong SecurityConfig (như .requestMatchers(...).hasRole(...)).
 * Thay vào đó, nó sẽ kiểm tra quyền truy cập dựa trên logic động:
 * 1. Lấy Role của user đang đăng nhập.
 * 2. Lấy danh sách Permission (URL + Method) gắn liền với Role đó (trong Enum).
 * 3. So khớp với Request hiện tại.
 */
@SuppressWarnings("deprecation")
@Component
public class DynamicAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private static final PathPatternParser parser = new PathPatternParser();

    /**
     * Phương thức quyết định xem Request có được phép đi tiếp hay không.
     *
     * @param authentication Supplier cung cấp thông tin user hiện tại (đã xác thực từ Filter).
     * @param context        Context chứa thông tin về Request HTTP (URL, Method...).
     * @return {@link AuthorizationDecision} chứa kết quả: true (cho phép) hoặc false (chặn - 403 Forbidden).
     */
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        // 1. Lấy thông tin Request
        String requestPath = context.getRequest().getRequestURI();
        String requestMethod = context.getRequest().getMethod();


        // 2. Lấy thông tin User (Authentication)
        Authentication auth = authentication.get();
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        // Lấy Role từ Authentication
        String roleName = auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
        // 3. Logic Check Enum
        try {
            Role role = Role.valueOf(roleName);
            PathContainer pathContainer = PathContainer.parsePath(requestPath);
            boolean allowed = role.getPermissions().stream()
                    .anyMatch(p -> {
                        PathPattern pattern = parser.parse(p.getUrl());
                        return pattern.matches(pathContainer)
                                && p.getMethod().equalsIgnoreCase(requestMethod);
                    });
            return new AuthorizationDecision(allowed);
        } catch (IllegalArgumentException e) {
            // Trường hợp Role trong Token không khớp với bất kỳ Role nào trong Enum
            return new AuthorizationDecision(false);
        }
    }
}
