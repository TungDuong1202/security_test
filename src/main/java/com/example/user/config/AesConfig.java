package com.example.user.config;

import com.example.user.utils.AesUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
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

    /**
     * Thêm @Bean ở đây.
     * Spring sẽ gọi hàm này, lấy kết quả (SecretKey) và bỏ vào ApplicationContext.
     * Khi Converter (hoặc bất kỳ đâu) cần SecretKey, Spring sẽ lấy từ đây đưa sang.
     */
    @Bean
    public SecretKey aesSecretKey() {
        return AesUtil.loadKeyFromBase64(aesKeyBase64);
    }
}
