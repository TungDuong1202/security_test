package com.example.user.utils;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * Tiện ích mã hóa đối xứng (Symmetric Encryption) sử dụng thuật toán AES.
 * <p>
 * Cấu hình bảo mật: <b>AES/GCM/NoPadding</b>
 * <ul>
 * <li><b>AES:</b> Advanced Encryption Standard (Chuẩn mã hóa nâng cao).</li>
 * <li><b>GCM (Galois/Counter Mode):</b> Chế độ hoạt động hiện đại, bảo mật cao hơn CBC.
 * Nó cung cấp cả tính bảo mật (Confidentiality) và tính toàn vẹn (Integrity).
 * Nghĩa là nếu dữ liệu bị hacker sửa đổi dù chỉ 1 bit, quá trình giải mã sẽ báo lỗi ngay.</li>
 * <li><b>NoPadding:</b> GCM hoạt động như stream cipher nên không cần padding như ECB/CBC.</li>
 * </ul>
 */
public final class AesUtil {

    private static final String AES = "AES";

    private static final String AES_GCM = "AES/GCM/NoPadding";

    private static final int AES_KEY_SIZE = 256;

    private static final int GCM_IV_LENGTH = 12;

    private static final int GCM_TAG_LENGTH = 128;

    private AesUtil(){}

    /**
     * Khôi phục {@link SecretKey} từ chuỗi Base64.
     * <p>
     * Thường dùng để load AES key từ file cấu hình hoặc biến môi trường.
     *
     * @param base64Key Chuỗi AES key dạng Base64 (sau khi decode phải đúng 16/24/32 bytes).
     * @return {@link SecretKey} dùng cho AES.
     *
     * @throws SecurityInternalException
     *         Nếu key không phải Base64 hợp lệ hoặc cấu hình key không đúng.
     */
    public static SecretKey loadKeyFromBase64(String base64Key) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64Key);
            return new SecretKeySpec(decodedKey, AES);
        } catch (IllegalArgumentException e) {
            throw new SecurityInternalException("Invalid Base64 AES key", e);
        } catch (Exception e) {
            throw new SecurityInternalException("Unexpected load AES key error", e);
        }
    }

    /**
     * Mã hóa dữ liệu bằng AES/GCM.
     * <p>
     * Quy trình:
     * <ol>
     *   <li>Sinh IV ngẫu nhiên (12 bytes).</li>
     *   <li>Khởi tạo Cipher với AES/GCM.</li>
     *   <li>Mã hóa dữ liệu và sinh AuthTag.</li>
     *   <li>Ghép IV vào đầu kết quả và encode Base64.</li>
     * </ol>
     *
     * @param plainText Chuỗi dữ liệu gốc cần mã hóa.
     * @param key       Khóa bí mật AES.
     * @return Chuỗi Base64 chứa: IV + CipherText + AuthTag.
     *
     * @throws SecurityBadRequestException
     *         Nếu dữ liệu đầu vào không hợp lệ.
     * @throws SecurityInternalException
     *         Nếu có lỗi cấu hình security hoặc môi trường không hỗ trợ AES/GCM.
     */
    public static String encrypt(String plainText, SecretKey key) {
        if (Objects.isNull(plainText)) {
            throw new SecurityBadRequestException("Plain text must not be null or empty");
        }
        if (Objects.isNull(key)) {
            throw new SecurityBadRequestException("SecretKey must not be null");
        }
        try {
            // 1. Sinh IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // 2. Init Cipher
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            // 3. Encrypt
            byte[] cipherText = cipher.doFinal(
                    plainText.getBytes(StandardCharsets.UTF_8)
            );

            // 4. Ghép IV + CipherText
            byte[] encrypted = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

            // 5. Encode Base64
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new SecurityInternalException("Invalid AES algorithm configuration", e);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityInternalException("AES/GCM algorithm not supported", e);

        }  catch (Exception e) {
            throw new SecurityInternalException("Unexpected encryption error", e);
        }
    }

    /**
     * Giải mã dữ liệu (Decryption).
     * <p>
     * Quy trình:
     * 1. Giải mã Base64.
     * 2. Tách 12 bytes đầu tiên ra làm IV.
     * 3. Phần còn lại là CipherText.
     * 4. Dùng Key + IV để giải mã.
     *
     * @param encryptedBase64 Chuỗi Base64 nhận được (chứa IV + CipherText).
     * @param key             Khóa bí mật AES (phải khớp với khóa lúc mã hóa).
     * @return Chuỗi văn bản gốc (Plain text).
     * @throws SecurityBadRequestException
     *         Nếu dữ liệu không hợp lệ, sai định dạng, sai key hoặc dữ liệu bị sửa đổi.
     * @throws SecurityInternalException
     *         Nếu lỗi cấu hình security hoặc hệ thống không hỗ trợ AES/GCM.
     */
    public static String decrypt(String encryptedBase64, SecretKey key) {
        try {
            // 1. Decode Base64
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);

            // 2. Validate độ dài (Phải lớn hơn IV)
            if (decoded.length <= GCM_IV_LENGTH) {
                throw new SecurityBadRequestException("Encrypted data is invalid (Too short)");
            }

            // 3. Tách IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, iv.length);

            // 4. Tách CipherText
            byte[] cipherText = new byte[decoded.length - GCM_IV_LENGTH];
            System.arraycopy(decoded, iv.length, cipherText, 0, cipherText.length);

            // 5. Init Cipher
            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            // 6. Decrypt
            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            // Lỗi: Input không phải Base64
            throw new SecurityBadRequestException("Invalid Base64 encrypted data", e);

        } catch (BadPaddingException | IllegalBlockSizeException e) {
            // Lỗi: Sai Key hoặc Dữ liệu bị sửa
            throw new SecurityBadRequestException("Encrypted data is tampered or key is incorrect", e);

        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            // Lỗi: Key null, sai độ dài, hoặc IV lỗi -> Thường do code/cấu hình sai
            throw new SecurityInternalException("Invalid security configuration", e);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            // Lỗi: Server không hỗ trợ thuật toán
            throw new SecurityInternalException("AES/GCM algorithm not supported", e);

        } catch (Exception e) {
            // Lỗi khác chưa lường trước
            throw new SecurityInternalException("Unexpected decryption error", e);
        }
    }
}
