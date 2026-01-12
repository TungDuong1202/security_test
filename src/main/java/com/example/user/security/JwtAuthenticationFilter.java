package com.example.user.security;

import com.example.user.constant.PublicEndpoints;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.http.server.PathContainer;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Bộ lọc xác thực JWT (JWT Authentication Filter).
 * <p>
 * Filter này sẽ chặn mọi request gửi đến Server để kiểm tra tính hợp lệ của Token.
 * Nó kế thừa {@link OncePerRequestFilter} để đảm bảo logic chỉ chạy đúng 1 lần cho mỗi request.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private static final String HEADER_STRING = "Authorization";
    private static final String START_HEADER = "Bearer ";
    private static final int START_INDEX_JWT = 7;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    // Danh sách lưu trữ các URL pattern đã được compile (biên dịch) sẵn
    // Giúp tăng tốc độ so sánh URL thay vì xử lý chuỗi String thuần túy.
    private final List<PublicEndpointMatcher> publicMatchers = new ArrayList<>();
    // Record nội bộ dùng để lưu cặp Pattern và Method
    private record PublicEndpointMatcher(PathPattern pattern, HttpMethod method) {}

    /**
     * Constructor: Inject dependencies và thực hiện Pre-processing (Tiền xử lý).
     * <p>
     * Thay vì parse URL patterns mỗi khi có request (gây chậm), ta thực hiện parse
     * toàn bộ danh sách Public Endpoints ngay khi ứng dụng khởi động (Start-up).
     */
    public JwtAuthenticationFilter(JwtUtils jwtUtils, AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtUtils = jwtUtils;
        this.authenticationEntryPoint = authenticationEntryPoint;

        // Khởi tạo Parser
        PathPatternParser parser = PathPatternParser.defaultInstance;

        // Loop qua file config string và biến nó thành Object PathPattern
        for (PublicEndpoints.Endpoint endpoint : PublicEndpoints.PUBLIC_API) {
            publicMatchers.add(
                    new PublicEndpointMatcher(
                            parser.parse(endpoint.getPattern()),
                            endpoint.getMethod()
                    )
            );
        }
    }

    /**
     * Quyết định xem Filter này có nên BỎ QUA request hiện tại không?
     * <p>
     * Nếu return <b>true</b>: Request là Public (Login, Register, Swagger...) -> Bỏ qua Filter, cho đi tiếp.
     * Nếu return <b>false</b>: Request là Private -> Chạy vào hàm {@code doFilterInternal} để check Token.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Chuyển String path sang PathContainer
        PathContainer path = PathContainer.parsePath(request.getServletPath());
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        return publicMatchers.stream().anyMatch(m ->
                m.pattern().matches(path)
                        && (Objects.isNull(m.method()) || m.method().equals(method))
        );
    }

    /**
     * Logic chính để xác thực Token.
     * <p>
     * Hàm này chỉ chạy khi {@code shouldNotFilter} trả về false.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws IOException, ServletException {

        String header = request.getHeader(HEADER_STRING);

        if (Objects.isNull(header)  || !header.startsWith(START_HEADER)) {
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new AuthenticationCredentialsNotFoundException("TOKEN_MISSING")
            );
            return;
        }

        try {
            String token = header.substring(START_INDEX_JWT);
            Claims claims = jwtUtils.parseToken(token);

            Long userId = claims.get("userId", Long.class);
            String role = claims.get("role", String.class);

            //Tạo đối tượng Authentication chuẩn của Spring Security
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );
            // Gắn thêm thông tin chi tiết của request (IP, Session ID...) vào Authentication
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // Lưu Authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Cho phép request đi tiếp
            chain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new CredentialsExpiredException("TOKEN_EXPIRED")
            );
        } catch (JwtException | IllegalArgumentException ex) {
            // Xử lý các lỗi khác: Token rác, sai chữ ký, Token rỗng...
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("TOKEN_INVALID")
            );

        } catch (AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, ex);
        }

    }

}
