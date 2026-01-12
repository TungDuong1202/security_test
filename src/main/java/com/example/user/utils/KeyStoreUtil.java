package com.example.user.utils;

import com.example.user.exception.SecurityConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Objects;

/**
 * Lớp tiện ích (Utility Class) hỗ trợ làm việc với KeyStore (Kho chứa khóa).
 * <p>
 * Class này chịu trách nhiệm tải file PKCS12 (.p12) từ thư mục resources và trích xuất cặp khóa RSA.
 * Vì đây là các thao tác cấu hình hệ thống, mọi lỗi xảy ra đều được coi là {@link SecurityConfigException}.
 */
public final class KeyStoreUtil {

    private static final String KEYSTORE_TYPE = "PKCS12";

    private KeyStoreUtil() {}

    /**
     * Tải file KeyStore từ classpath (thư mục resources).
     *
     * @param path     Đường dẫn file keystore (ví dụ: "keystore.p12").
     * @param password Mật khẩu để mở file keystore.
     * @return Đối tượng {@link KeyStore} đã được load dữ liệu vào bộ nhớ.
     * @throws SecurityConfigException Ném ra nếu quá trình tải thất bại (Lỗi 500):
     * <ul>
     * <li><b>Không tìm thấy file:</b> Nếu file không tồn tại trong classpath.</li>
     * <li><b>Sai mật khẩu:</b> Nếu mật khẩu keystore không đúng (IOException).</li>
     * <li><b>Sai định dạng:</b> Nếu file bị hỏng hoặc chứng chỉ lỗi (CertificateException).</li>
     * <li><b>Lỗi thuật toán:</b> Nếu server không hỗ trợ loại PKCS12 (NoSuchAlgorithmException).</li>
     * </ul>
     */
    public static KeyStore loadKeyStore(String path, char[] password) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {

            if (Objects.isNull(is)) {
                throw new SecurityConfigException("Keystore file not found in classpath: " + path);
            }

            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(is, password);
            return keyStore;

        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            // Lỗi Hệ thống: JVM hoặc Server không hỗ trợ định dạng PKCS12 hoặc thuật toán mã hóa
            throw new SecurityConfigException("System Error: Keystore type (PKCS12) or algorithm not supported", e);
        } catch (CertificateException e) {
            // Lỗi Cấu hình: File keystore bị hỏng hoặc chứng chỉ bên trong sai định dạng
            throw new SecurityConfigException("Configuration Error: Invalid certificate format in keystore", e);
        } catch (IOException e) {
            // IOException thường xảy ra khi sai password hoặc file bị lỗi I/O
            if (e.getCause() instanceof UnrecoverableKeyException) {
                throw new SecurityConfigException("Configuration Error: Invalid keystore password", e);
            }
            // Hoặc do file bị lỗi đọc ghi
            throw new SecurityConfigException("Configuration Error: Unable to load keystore file (I/O Error or Wrong Password)", e);
        } catch (Exception e) {
            throw new SecurityConfigException("Unexpected Error: Failed to load KeyStore", e);
        }
    }

    /**
     * Lấy Private Key từ KeyStore đã load.
     *
     * @param keyStore    Đối tượng KeyStore.
     * @param alias       Tên định danh (Alias) của Private Key.
     * @param keyPassword Mật khẩu bảo vệ Private Key (thường trùng với mật khẩu KeyStore).
     * @return {@link PrivateKey} dùng để ký số (Sign) hoặc giải mã (Decrypt).
     * @throws SecurityConfigException Ném ra nếu không lấy được Key (Lỗi 500):
     * <ul>
     * <li><b>Không tìm thấy Alias:</b> Alias không tồn tại hoặc Key tại đó không phải PrivateKey.</li>
     * <li><b>Sai mật khẩu Key:</b> Mật khẩu riêng của Key bị sai (UnrecoverableKeyException).</li>
     * <li><b>Lỗi KeyStore:</b> KeyStore chưa được khởi tạo đúng cách.</li>
     * </ul>
     */
    public static PrivateKey getPrivateKey(KeyStore keyStore, String alias, char[] keyPassword) {
        try {
            Key key = keyStore.getKey(alias, keyPassword);

            if (key instanceof PrivateKey privateKey) {
                return privateKey;
            }
            // Tìm thấy Alias nhưng nó không chứa Private Key (có thể là Cert hoặc SecretKey)
            throw new SecurityConfigException("Configuration Error: Private key not found or invalid type for alias: " + alias);

        } catch (UnrecoverableKeyException e) {
            // Lỗi Mật khẩu: Sai password của riêng Private Key (Lưu ý: Key có thể có password khác với Keystore)
            throw new SecurityConfigException("Configuration Error: Invalid password for Private Key (Alias: " + alias + ")", e);
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            // Lỗi Hệ thống: Không thể truy cập cơ chế Keystore
            throw new SecurityConfigException("System Error: Unable to access keystore mechanism", e);
        } catch (Exception e) {
            throw new SecurityConfigException("Unexpected Error: Failed to retrieve Private Key", e);
        }
    }

    /**
     * Lấy Public Key từ Certificate nằm trong KeyStore.
     *
     * @param keyStore Đối tượng KeyStore.
     * @param alias    Tên định danh (Alias) chứa Certificate.
     * @return {@link PublicKey} dùng để xác thực chữ ký (Verify) hoặc mã hóa (Encrypt).
     * @throws SecurityConfigException Ném ra nếu không lấy được Key (Lỗi 500):
     * <ul>
     * <li><b>Không tìm thấy Cert:</b> Alias không tồn tại hoặc không chứa Certificate.</li>
     * <li><b>Lỗi KeyStore:</b> KeyStore bị lỗi truy cập.</li>
     * </ul>
     */
    public static PublicKey getPublicKey(KeyStore keyStore, String alias) {
        try {
            Certificate cert = keyStore.getCertificate(alias);

            if (Objects.isNull(cert)) {
                // Lỗi Cấu hình: Alias không tồn tại hoặc alias đó không chứa Certificate
                throw new SecurityConfigException("Configuration Error: Certificate not found for alias: " + alias);
            }

            return cert.getPublicKey();

        } catch (KeyStoreException e) {
            // Lỗi Hệ thống: Keystore chưa được khởi tạo hoặc bị đóng
            throw new SecurityConfigException("System Error: Unable to access certificate in keystore", e);

        } catch (Exception e) {

            throw new SecurityConfigException("Unexpected Error: Failed to retrieve Public Key", e);
        }
    }
}