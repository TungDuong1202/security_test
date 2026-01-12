package com.example.user.utils;

import com.example.user.exception.SecurityConfigException;
import com.example.user.exception.SecurityProcessException;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Class tiện ích hỗ trợ mã hóa đối xứng (Symmetric Encryption) sử dụng thuật toán AES.
 * <p>
 * Cấu hình bảo mật: <b>AES/GCM/NoPadding</b>
 */
public final class AesUtil {

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private AesUtil() {}

    /**
     * Khôi phục đối tượng {@link SecretKey} từ chuỗi Base64.
     *
     * @param base64Key Chuỗi AES Key dạng Base64.
     * @return Đối tượng {@link SecretKey}.
     * @throws SecurityConfigException Nếu chuỗi không phải Base64 hoặc có lỗi bất ngờ khác.
     */
    public static SecretKey loadKeyFromBase64(String base64Key) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64Key);
            return new SecretKeySpec(decodedKey, AES);
        } catch (IllegalArgumentException e) {
            throw new SecurityConfigException("Invalid Key Configuration: Input string is not valid Base64", e);
        } catch (Exception e) {
            throw new SecurityConfigException("Unexpected Error when load Key AES", e);
        }
    }

    /**
     * Mã hóa văn bản gốc (Encryption).
     *
     * @param plainText Chuỗi văn bản gốc.
     * @param key       Khóa bí mật AES.
     * @return Chuỗi Base64 chứa [IV + CipherText].
     * @throws SecurityConfigException Ném ra nếu có lỗi hệ thống hoặc cấu hình sai (Lỗi 500):
     * <ul>
     * <li><b>Lỗi hệ thống:</b> Nếu JVM không hỗ trợ thuật toán "AES/GCM/NoPadding" (NoSuchAlgorithmException).</li>
     * <li><b>Key không hợp lệ:</b> Nếu Key bị null hoặc độ dài không đúng chuẩn (InvalidKeyException).</li>
     * <li><b>IV không hợp lệ:</b> Nếu tham số khởi tạo IV bị lỗi (InvalidAlgorithmParameterException).</li>
     * </ul>
     * @throws SecurityProcessException Ném ra nếu quá trình mã hóa bị lỗi (Lỗi 400 - hiếm gặp khi Encrypt):
     * <ul>
     * <li>Nếu kích thước khối dữ liệu không hợp lệ (IllegalBlockSizeException).</li>
     * <li>Nếu xảy ra lỗi padding (BadPaddingException).</li>
     * </ul>
     */
    public static String encrypt(String plainText, SecretKey key) {
        if (plainText == null) return null;

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] encrypted = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encrypted);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityConfigException("System Error: AES/GCM/NoPadding algorithm not available", e);

        } catch (InvalidKeyException e) {
            throw new SecurityConfigException("Configuration Error: AES Key is null or invalid length", e);

        } catch (InvalidAlgorithmParameterException e) {
            throw new SecurityConfigException("Configuration Error: Invalid IV or Tag length parameters", e);

        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SecurityProcessException("Processing Error: Unable to encrypt data block", e);

        } catch (Exception e) {
            throw new SecurityConfigException("Unexpected Error: An unknown error occurred during the data encryption process.", e);
        }
    }

    /**
     * Giải mã chuỗi Base64 (Decryption).
     *
     * @param encryptedBase64 Chuỗi Base64 chứa IV và CipherText.
     * @param key             Khóa bí mật AES.
     * @return Chuỗi văn bản gốc.
     * @throws SecurityProcessException Ném ra nếu dữ liệu đầu vào không hợp lệ hoặc bị từ chối (Lỗi Client/Data - 400):
     * <ul>
     * <li><b>Sai định dạng:</b> Nếu <code>encryptedBase64</code> không phải là chuỗi Base64 hợp lệ.</li>
     * <li><b>Dữ liệu hỏng:</b> Nếu dữ liệu giải mã quá ngắn (thiếu IV).</li>
     * <li><b>Sai Key:</b> Nếu Key giải mã không khớp với Key mã hóa.</li>
     * <li><b>Bị giả mạo (Tampering):</b> Nếu dữ liệu đã bị bên thứ 3 sửa đổi (Integrity Check Failed).</li>
     * </ul>
     * @throws SecurityConfigException Ném ra nếu có lỗi cấu hình hệ thống (Lỗi Server - 500):
     * <ul>
     * <li><b>Lỗi hệ thống:</b> Nếu thuật toán giải mã không khả dụng.</li>
     * <li><b>Cấu hình sai:</b> Nếu tham số Key hoặc IV không hợp lệ.</li>
     * </ul>
     */
    public static String decrypt(String encryptedBase64, SecretKey key) {
        if (encryptedBase64 == null) return null;

        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedBase64);

            if (decoded.length <= GCM_IV_LENGTH) {
                throw new SecurityProcessException("Invalid encrypted data: Content too short, missing IV");
            }

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, iv.length);

            byte[] cipherText = new byte[decoded.length - GCM_IV_LENGTH];
            System.arraycopy(decoded, iv.length, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            throw new SecurityProcessException("Input data is not valid Base64", e);

        } catch (BadPaddingException e) {
            throw new SecurityProcessException("Decryption failed: Integrity check failed (Wrong key or data tampered)", e);

        } catch (IllegalBlockSizeException e) {
            throw new SecurityProcessException("Data corruption: Invalid block size", e);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityConfigException("System Error: Decryption algorithm not available", e);

        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new SecurityConfigException("Configuration Error: Invalid Key or IV parameters", e);

        } catch (Exception e) {
            throw new SecurityConfigException("Unexpected Error: An unknown error occurred while decoding the data.", e);
        }
    }
}