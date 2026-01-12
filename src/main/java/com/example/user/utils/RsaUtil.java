package com.example.user.utils;

import com.example.user.exception.SecurityConfigException;
import com.example.user.exception.SecurityProcessException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * Tiện ích hỗ trợ mã hóa và ký số bằng thuật toán RSA (Bất đối xứng).
 * <p>
 * Class này được thiết kế cho mô hình <b>Hybrid Encryption</b> (Mã hóa lai):
 * <ul>
 * <li><b>Mã hóa (Encryption):</b> Dùng Public Key để mã hóa dữ liệu nhỏ (thường là AES Key hoặc Token ngắn).
 * <b>Lưu ý:</b> RSA không thể mã hóa dữ liệu lớn hơn kích thước Key (VD: Key 2048 bit chỉ mã hóa được tối đa ~245 bytes).</li>
 * <li><b>Ký số (Signing):</b> Dùng Private Key để tạo chữ ký, đảm bảo tính toàn vẹn và xác thực nguồn gốc.</li>
 * </ul>
 */
public final class RsaUtil {

    private static final String RSA_CIPHER = "RSA/ECB/PKCS1Padding";
    private static final String SIGN_ALGORITHM = "SHA256withRSA";

    private RsaUtil() {}

    /**
     * Mã hóa dữ liệu bằng RSA Public Key.
     * <p>
     * Thường dùng để gửi dữ liệu bí mật (như mật khẩu, AES Key) lên server.
     * Chỉ người nắm giữ Private Key (Server) mới giải mã được.
     *
     * @param plainText Dữ liệu gốc cần mã hóa.
     * @param publicKey Khóa công khai của người nhận.
     * @return Chuỗi dữ liệu đã mã hóa (Base64).
     * @throws SecurityProcessException Ném ra nếu dữ liệu đầu vào không hợp lệ (Lỗi 400):
     * <ul>
     * <li><b>Dữ liệu quá dài:</b> Nếu độ dài chuỗi vượt quá giới hạn của RSA Key (IllegalBlockSizeException).</li>
     * </ul>
     * @throws SecurityConfigException Ném ra nếu lỗi cấu hình hệ thống (Lỗi 500):
     * <ul>
     * <li><b>Lỗi thuật toán:</b> Server không hỗ trợ RSA (NoSuchAlgorithmException).</li>
     * <li><b>Key hỏng:</b> Public Key đầu vào bị lỗi hoặc null (InvalidKeyException).</li>
     * </ul>
     */
    public static String encrypt(String plainText, PublicKey publicKey) {
        if (plainText == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(RSA_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (IllegalBlockSizeException e) {
            // [QUAN TRỌNG] RSA có giới hạn kích thước (Key 2048 bit chỉ mã hóa được ~245 bytes)
            throw new SecurityProcessException("Encryption failed: Data length exceeds RSA limit", e);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityConfigException("System Error: RSA algorithm not available", e);

        } catch (InvalidKeyException e) {
            throw new SecurityConfigException("Configuration Error: Invalid RSA Public Key", e);

        } catch (Exception e) {
            throw new SecurityConfigException("Unexpected Error: RSA Encryption failed", e);
        }
    }

    /**
     * Giải mã dữ liệu bằng RSA Private Key.
     * <p>
     * Dùng để giải mã các gói tin được gửi từ Client (đã mã hóa bằng Public Key của Server).
     *
     * @param encryptedBase64 Chuỗi dữ liệu đã mã hóa (Base64).
     * @param privateKey      Khóa bí mật của server.
     * @return Dữ liệu gốc (Plain text).
     * @throws SecurityProcessException Ném ra nếu dữ liệu rác hoặc sai Key (Lỗi 400):
     * <ul>
     * <li><b>Sai định dạng:</b> Input không phải là Base64 hợp lệ (IllegalArgumentException).</li>
     * <li><b>Giải mã thất bại:</b> Dữ liệu bị hỏng hoặc mã hóa bằng Key không khớp (BadPaddingException).</li>
     * <li><b>Block lỗi:</b> Kích thước khối dữ liệu không đúng chuẩn RSA (IllegalBlockSizeException).</li>
     * </ul>
     * @throws SecurityConfigException Ném ra nếu lỗi hệ thống (Lỗi 500):
     * <ul>
     * <li><b>Key hỏng:</b> Private Key cấu hình sai (InvalidKeyException).</li>
     * <li><b>Lỗi thuật toán:</b> Server không hỗ trợ RSA.</li>
     * </ul>
     */
    public static String decrypt(String encryptedBase64, PrivateKey privateKey) {
        if (encryptedBase64 == null) return null;
        try {
            // 1. Decode Base64
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);

            // 2. Decrypt
            Cipher cipher = Cipher.getInstance(RSA_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            throw new SecurityProcessException("Input data is not valid Base64", e);

        } catch (BadPaddingException e) {
            // Đây là lỗi phổ biến nhất khi dùng sai cặp Key (Private Key này không mở được gói tin kia)
            throw new SecurityProcessException("Decryption failed: Invalid padding (Wrong key or corrupted data)", e);

        } catch (IllegalBlockSizeException e) {
            throw new SecurityProcessException("Decryption failed: Invalid block size", e);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityConfigException("System Error: RSA algorithm not available", e);

        } catch (InvalidKeyException e) {
            throw new SecurityConfigException("Configuration Error: Invalid RSA Private Key", e);

        } catch (Exception e) {

            throw new SecurityConfigException("Unexpected Error: RSA Decryption failed", e);
        }
    }

    /**
     * Tạo chữ ký số (Sign) cho dữ liệu.
     * <p>
     * Dùng Private Key để ký, đảm bảo dữ liệu này xuất phát từ chính chủ và không bị sửa đổi.
     *
     * @param data       Dữ liệu cần ký.
     * @param privateKey Khóa bí mật của người gửi.
     * @return Chữ ký số dạng Base64.
     * @throws SecurityConfigException Ném ra nếu quá trình ký thất bại (Lỗi 500):
     * <ul>
     * <li><b>Lỗi thuật toán:</b> Server không hỗ trợ SHA256withRSA.</li>
     * <li><b>Key hỏng:</b> Private Key không hợp lệ để ký.</li>
     * <li><b>Lỗi hệ thống:</b> Quá trình ký bị gián đoạn.</li>
     * </ul>
     */
    public static String sign(String data, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            byte[] signBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new SecurityConfigException("System Error: Signing algorithm (SHA256withRSA) not available", e);

        } catch (InvalidKeyException e) {
            throw new SecurityConfigException("Configuration Error: Invalid RSA Private Key for signing", e);

        } catch (SignatureException e) {
            throw new SecurityConfigException("System Error: Signing process failed", e);

        } catch (Exception e) {
            throw new SecurityConfigException("Unexpected Error: RSA Signing failed", e);
        }
    }

    /**
     * Xác thực chữ ký số (Verify).
     *
     * @param data            Dữ liệu gốc.
     * @param signatureBase64 Chữ ký số nhận được (Base64).
     * @param publicKey       Khóa công khai của người gửi.
     * @return {@code true} nếu chữ ký hợp lệ, {@code false} nếu chữ ký không khớp hoặc lỗi format.
     * @throws SecurityProcessException Nếu chữ ký gửi lên sai định dạng Base64 (Lỗi 400).
     * @throws SecurityConfigException Nếu lỗi hệ thống hoặc Key hỏng (Lỗi 500).
     */
    public static boolean verify(String data, String signatureBase64, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            byte[] signBytes = Base64.getDecoder().decode(signatureBase64);
            return signature.verify(signBytes);

        } catch (IllegalArgumentException e) {
            // Lỗi Client: Gửi chữ ký rác, không phải Base64 -> Báo lỗi 400
            throw new SecurityProcessException("Invalid signature format: Not a Base64 string", e);

        } catch (SignatureException e) {
            // Lỗi Format chữ ký (Base64 đúng nhưng nội dung byte bên trong không phải chữ ký RSA)
            // Trả về false để coi như xác thực thất bại
            return false;

        } catch (NoSuchAlgorithmException e) {
            throw new SecurityConfigException("System Error: Verification algorithm not available", e);

        } catch (InvalidKeyException e) {
            throw new SecurityConfigException("Configuration Error: Invalid RSA Public Key for verification", e);

        } catch (Exception e) {
            throw new SecurityConfigException("Unexpected Error: Signature verification failed", e);
        }
    }
}