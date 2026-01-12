package com.example.user.config;

import com.example.user.utils.KeyStoreUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Cấu hình hệ thống mã hóa bất đối xứng (RSA Cryptography Configuration).
 * <p>
 * Class này chịu trách nhiệm:
 * 1. Đọc thông tin cấu hình KeyStore từ file {@code application.properties}.
 * 2. Tải file KeyStore (.p12 hoặc .jks) vào bộ nhớ.
 * 3. Trích xuất cặp khóa (Private Key & Public Key) và đăng ký chúng thành các {@link Bean}.
 * <p>
 * Các Bean này sau đó sẽ được tự động Inject vào {@code JwtUtils} hoặc các service mã hóa khác.
 */
@Configuration
public class RsaConfig {

    @Value("${crypto.rsa.keystore-path}")
    private String keystorePath;

    @Value("${crypto.rsa.keystore-password}")
    private String keystorePassword;

    @Value("${crypto.rsa.alias}")
    private String alias;

    /**
     * Bean khởi tạo KeyStore Object.
     * <p>
     * Bean này chỉ được tạo 1 lần duy nhất khi ứng dụng khởi động.
     * Nó load toàn bộ file .p12 vào RAM để các hàm lấy key phía sau không phải đọc ổ cứng lại.
     *
     * @return Đối tượng {@link KeyStore} chứa các chứng chỉ và khóa.
     */
    @Bean
    public KeyStore keyStore() {
        return KeyStoreUtil.loadKeyStore(
                keystorePath,
                keystorePassword.toCharArray()
        );
    }

    /**
     * Bean cung cấp RSA Private Key.
     * <p>
     * Spring sẽ tự động tìm Bean {@code keyStore} ở trên để inject vào tham số của hàm này.
     *
     * @param keyStore Bean KeyStore đã được khởi tạo trước đó.
     * @return {@link PrivateKey} dùng để <b>KÝ (Sign)</b> Token JWT.
     */
    @Bean
    public PrivateKey privateKey(KeyStore keyStore) {
        return KeyStoreUtil.getPrivateKey(
                keyStore,
                alias,
                keystorePassword.toCharArray()
        );
    }

    /**
     * Bean cung cấp RSA Public Key.
     * <p>
     * Spring cũng tự động inject Bean {@code keyStore} vào đây.
     *
     * @param keyStore Bean KeyStore đã được khởi tạo.
     * @return {@link PublicKey} dùng để <b>XÁC THỰC (Verify)</b> Token JWT.
     */
    @Bean
    public PublicKey publicKey(KeyStore keyStore) {
        return KeyStoreUtil.getPublicKey(keyStore, alias);
    }
}

