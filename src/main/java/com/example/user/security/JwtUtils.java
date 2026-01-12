package com.example.user.security;

import com.example.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;

/**
 * Class tiện ích (Utility) chịu trách nhiệm xử lý các thao tác liên quan đến JSON Web Token (JWT).
 * <p>
 * Class này sử dụng cơ chế mã hóa bất đối xứng (Asymmetric Encryption) thuật toán RS256:
 * <ul>
 * <li><b>Private Key:</b> Được sử dụng để KÝ (Sign) tạo ra token mới. Key này cần được bảo mật tuyệt đối.</li>
 * <li><b>Public Key:</b> Được sử dụng để XÁC THỰC (Verify) chữ ký của token nhận được.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class JwtUtils {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private static final String CLAIM_USERID = "userId";
    private static final String CLAIM_ROLE = "role";

    private static final long ACCESSTOKENEXPIRATIONMS = 60 * 60 * 1000; // 1h

    /**
     * Tạo ra một chuỗi Access Token (JWT) cho người dùng.
     * <p>
     * Token này chứa các thông tin (Claims) về danh tính và quyền hạn của người dùng,
     * được ký số bằng <b>Private Key</b> của server để đảm bảo tính toàn vẹn.
     *
     * @param userId ID định danh duy nhất của người dùng (thường là Primary Key trong DB).
     * @param role   Mã quyền hạn của người dùng (Ví dụ: "ADMIN", "USER", "STAFF").
     * @return Một chuỗi String JWT đã được ký (Compact JWS).
     */
    public String generateAccessToken(Long userId, String role){
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(CLAIM_USERID, userId)
                .claim(CLAIM_ROLE, role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + ACCESSTOKENEXPIRATIONMS)
                )
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * Phân tích (Parse) và xác thực tính hợp lệ của một chuỗi Token.
     * <p>
     * Hàm này sử dụng <b>Public Key</b> để kiểm tra chữ ký số (Signature) của token.
     * Nó đảm bảo rằng token này thực sự do Server tạo ra và nội dung bên trong chưa bị chỉnh sửa.
     *
     * @param token Chuỗi JWT cần kiểm tra (thường lấy từ header Authorization).
     * @return Đối tượng {@link Claims} chứa toàn bộ dữ liệu (payload) bên trong token (userId, role, exp...).
     * @throws io.jsonwebtoken.ExpiredJwtException Nếu token đã hết hạn.
     * @throws io.jsonwebtoken.security.SignatureException Nếu chữ ký không khớp (Token bị giả mạo).
     * @throws io.jsonwebtoken.MalformedJwtException Nếu token sai định dạng cấu trúc.
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
