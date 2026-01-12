package com.example.user.config;

import com.example.user.utils.AesUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

/**
 * Cấu hình tập trung cho mã hóa đối xứng AES.
 * <p>
 * Class này chịu trách nhiệm:
 * 1. Đọc chuỗi AES Key (dạng Base64) từ file cấu hình hoặc biến môi trường.
 * 2. Chuyển đổi chuỗi đó thành đối tượng {@link SecretKey} chuẩn của Java.
 * 3. Cung cấp Key này cho các Service khác sử dụng (thông qua getter).
 */
@Configuration
public class AesConfig {
    @Value("${crypto.aes.key}")
    private String aesKeyBase64;

    @Getter
    private SecretKey aesSecretKey;

    /**
     * Hàm khởi tạo chạy 1 lần duy nhất ngay sau khi Bean được tạo (Post-Construction).
     * <p>
     * Tại sao dùng @PostConstruct?
     * <ul>
     * <li>Để đảm bảo biến {@code aesKeyBase64} đã được Spring inject giá trị xong.</li>
     * <li>Để thực hiện logic giải mã Base64 -> SecretKey một lần duy nhất lúc khởi động server.
     * Các lần sau dùng lại {@code aesSecretKey} có sẵn, giúp tối ưu hiệu năng.</li>
     * </ul>
     */
    @PostConstruct
    public void init() {
        this.aesSecretKey = AesUtil.loadKeyFromBase64(aesKeyBase64);
    }
}
