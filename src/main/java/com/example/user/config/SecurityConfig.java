package com.example.user.config;

import com.example.user.constant.PublicEndpoints;
import com.example.user.security.exeption.CustomAccessDeniedHandler;
import com.example.user.security.exeption.CustomAuthenticationEntryPoint;
import com.example.user.security.DynamicAuthorizationManager;
import com.example.user.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Objects;

/**
 * Cấu hình bảo mật trung tâm (Security Configuration) cho ứng dụng.
 * <p>
 * Class này chịu trách nhiệm:
 * 1. Cung cấp cơ chế mã hóa mật khẩu.
 * 2. Định nghĩa các quy tắc truy cập (Ai được vào đâu).
 * 3. Cấu hình bộ lọc bảo mật (Filter Chain) để xử lý các request HTTP.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DynamicAuthorizationManager dynamicAuthorizationManager;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * Cung cấp Bean mã hóa mật khẩu (Password Encoder) cho toàn bộ ứng dụng.
     * <p>
     * Sử dụng thuật toán BCrypt (chuẩn công nghiệp hiện tại) để băm mật khẩu.
     * Bean này sẽ được Inject vào UserService để mã hóa pass khi đăng ký
     * và Spring Security dùng để kiểm tra pass khi đăng nhập.
     *
     * @return Đối tượng BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Thiết lập chuỗi bộ lọc bảo mật (Security Filter Chain).
     * <p>
     * Đây là nơi định nghĩa các quy tắc bảo mật HTTP.
     * Cấu hình hiện tại:
     * <ul>
     * <li>Tắt CSRF: Để dễ dàng test API bằng Postman/cURL.</li>
     * </ul>
     *
     * @param http Đối tượng cấu hình bảo mật HTTP của Spring.
     * @return Chuỗi bộ lọc đã được cấu hình.
     * @throws Exception Nếu có lỗi xảy ra trong quá trình thiết lập.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint) // 401
                        .accessDeniedHandler(accessDeniedHandler)             // 403
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> {
                    PublicEndpoints.PUBLIC_API.forEach(endpoint -> {
                        if (Objects.isNull(endpoint.getMethod())) {
                            // Nếu method null -> Cho phép tất cả method
                            auth.requestMatchers(endpoint.getPattern()).permitAll();
                        } else {
                            auth.requestMatchers(endpoint.getMethod(), endpoint.getPattern()).permitAll();
                        }
                    });

                    // 5. Các request còn lại -> Vào Dynamic Manager check quyền
                    auth.anyRequest().access(dynamicAuthorizationManager);
                });

        return http.build();
    }
}