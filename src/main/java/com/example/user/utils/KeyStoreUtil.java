package com.example.user.utils;

import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Objects;

/**
 * Lớp tiện ích (Utility Class) hỗ trợ làm việc với KeyStore (Kho chứa khóa).
 * <p>
 * Hỗ trợ tải file keystore từ classpath và trích xuất cặp khóa RSA (Public/Private Key).
 * Class này được thiết kế dạng stateless (tĩnh) để dễ dàng sử dụng ở mọi nơi.
 */
public final class KeyStoreUtil {

    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String KEYSTORE_NOT_FOUND = "Không tìm thấy file keystore tại đường dẫn classpath: %s";

    private KeyStoreUtil() {}

    /**
     * Tải KeyStore từ classpath.
     *
     * @param path     đường dẫn file keystore trong resources
     * @param password mật khẩu keystore
     * @return {@link KeyStore} đã được load
     * @throws SecurityInternalException nếu file không tồn tại, sai mật khẩu
     *                                 hoặc lỗi cấu hình hệ thống
     */
    public static KeyStore loadKeyStore(String path, char[] password) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path)) {

            if (Objects.isNull(is)) {
                throw new IllegalStateException(String.format(KEYSTORE_NOT_FOUND, path));
            }

            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(is, password);
            return keyStore;
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            // JVM / Provider không hỗ trợ keystore type hoặc thuật toán
            throw new SecurityInternalException("Keystore type or algorithm not supported", e);

        } catch (CertificateException e) {
            // File keystore hoặc certificate bên trong bị lỗi / sai định dạng
            throw new SecurityInternalException("Invalid keystore certificate format", e);

        } catch (Exception e) {
            throw new SecurityInternalException("Unexpected error while loading keystore", e);
        }
    }

    /**
     * Lấy Private Key từ KeyStore.
     *
     * @param keyStore    keystore đã load
     * @param alias       alias của private key
     * @param keyPassword mật khẩu private key
     * @return {@link PrivateKey} dùng để ký (sign)
     * @throws SecurityInternalException nếu alias không tồn tại,
     *                                 sai mật khẩu hoặc lỗi hệ thống
     */
    public static PrivateKey getPrivateKey(
            KeyStore keyStore,
            String alias,
            char[] keyPassword
    ) {
        try {
            Key key = keyStore.getKey(alias, keyPassword);

            if (key instanceof PrivateKey privateKey) {
                return privateKey;
            }
            throw new SecurityInternalException("Private key not found or invalid for alias: " + alias);

        } catch (UnrecoverableKeyException e) {
            // Sai mật khẩu private key
            throw new SecurityInternalException("Invalid private key password for alias: " + alias, e);

        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            // Keystore / thuật toán không truy cập được
            throw new SecurityInternalException("Unable to access private key from keystore", e);

        } catch (Exception e) {
            throw new SecurityInternalException("Unexpected error while retrieving private key", e);
        }
    }

    /**
     * Lấy Public Key từ Certificate trong KeyStore.
     *
     * @param keyStore keystore đã load
     * @param alias    alias của certificate
     * @return {@link PublicKey} dùng để verify
     * @throws SecurityInternalException nếu không tìm thấy certificate
     *                                 hoặc lỗi hệ thống
     */
    public static PublicKey getPublicKey(KeyStore keyStore, String alias) {
        try {
            Certificate cert = keyStore.getCertificate(alias);

            if (Objects.isNull(cert)) {
                throw new SecurityInternalException("Certificate not found for alias: " + alias);
            }

            return cert.getPublicKey();

        } catch (KeyStoreException e) {
            // Không truy cập được keystore
            throw new SecurityInternalException("Unable to access certificate from keystore", e);

        } catch (Exception e) {
            throw new SecurityInternalException("Unexpected error while retrieving public key", e);
        }
    }
}
