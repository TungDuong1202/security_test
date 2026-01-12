package com.example.user.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * Tiện ích hỗ trợ mã hóa và ký số bằng thuật toán RSA (Bất đối xứng).
 * <p>
 * Class này được thiết kế cho mô hình <b>Hybrid Encryption</b> (Mã hóa lai):
 * <ul>
 * <li><b>Mã hóa (Encryption):</b> Dùng Public Key để mã hóa các dữ liệu nhỏ (thường là AES Key).
 * Không dùng để mã hóa dữ liệu lớn vì RSA rất chậm và có giới hạn kích thước.</li>
 * <li><b>Ký số (Signing):</b> Dùng Private Key để tạo chữ ký, đảm bảo tính toàn vẹn và xác thực nguồn gốc dữ liệu.</li>
 * </ul>
 *
 * <b>Cấu hình kỹ thuật:</b>
 * <ul>
 * <li>Mã hóa: RSA/ECB/PKCS1Padding</li>
 * <li>Ký số: SHA256withRSA</li>
 * </ul>
 */
public final class RsaUtil {

    private static final String RSA = "RSA";
    private static final String RSA_CIPHER = "RSA/ECB/PKCS1Padding";
    private static final String SIGN_ALGORITHM = "SHA256withRSA";

    private RsaUtil() {}


    /**
     * Mã hóa dữ liệu bằng RSA Public Key.
     *
     * @param data      Dữ liệu cần mã hóa (thường là AES Secret Key).
     * @param publicKey Khóa công khai của người nhận.
     * @return Chuỗi dữ liệu đã mã hóa (Base64).
     *
     * @throws SecurityBadRequestException
     *         Nếu dữ liệu quá lớn, key không hợp lệ hoặc padding không đúng.
     * @throws SecurityInternalException
     *         Nếu lỗi hệ thống hoặc thuật toán không khả dụng.
     */
    public static String encryptWithPublicKey(
            byte[] data,
            PublicKey publicKey
    ) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(data);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SecurityBadRequestException("Invalid data size or RSA padding");

        } catch (InvalidKeyException e) {
            throw new SecurityBadRequestException("Invalid RSA public key");

        } catch (GeneralSecurityException e) {
            throw new SecurityInternalException("RSA encryption configuration error", e);

        } catch (Exception e) {
            throw new SecurityInternalException("Unexpected RSA encryption error", e);
        }
    }

    /**
     * Giải mã dữ liệu bằng RSA Private Key.
     *
     * @param encryptedBase64 Chuỗi dữ liệu đã mã hóa (Base64).
     * @param privateKey      Khóa bí mật của server.
     * @return Dữ liệu gốc sau khi giải mã.
     *
     * @throws SecurityBadRequestException
     *         Nếu dữ liệu Base64 không hợp lệ, key sai hoặc padding không đúng.
     * @throws SecurityInternalException
     *         Nếu lỗi hệ thống hoặc thuật toán.
     */
    public static byte[] decryptWithPrivateKey(
            String encryptedBase64,
            PrivateKey privateKey
    ) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);
            Cipher cipher = Cipher.getInstance(RSA_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encrypted);
        } catch (IllegalArgumentException e) {
            throw new SecurityBadRequestException("Invalid Base64 encrypted data");

        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SecurityBadRequestException("Invalid RSA encrypted payload");

        } catch (InvalidKeyException e) {
            throw new SecurityBadRequestException("Invalid RSA private key");

        } catch (GeneralSecurityException e) {
            throw new SecurityInternalException("RSA decryption configuration error", e);

        } catch (Exception e) {
            throw new SecurityInternalException("Unexpected RSA decryption error", e);
        }
    }


    /**
     * Tạo chữ ký số cho dữ liệu bằng RSA Private Key.
     *
     * @param data       Dữ liệu cần ký.
     * @param privateKey Khóa bí mật của người gửi.
     * @return Chữ ký số (Base64).
     *
     * @throws SecurityBadRequestException
     *         Nếu private key không hợp lệ hoặc dữ liệu không ký được.
     * @throws SecurityInternalException
     *         Nếu thuật toán ký không khả dụng.
     */
    public static String sign(String data, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (InvalidKeyException e) {
            throw new SecurityBadRequestException("Invalid RSA private key for signing");

        } catch (GeneralSecurityException e) {
            throw new SecurityInternalException("RSA signing configuration error", e);

        } catch (Exception e) {
            throw new SecurityInternalException("Unexpected RSA signing error", e);
        }
    }

    /**
     * Xác thực chữ ký số bằng RSA Public Key.
     *
     * @param data            Dữ liệu gốc.
     * @param signatureBase64 Chữ ký số (Base64).
     * @param publicKey       Khóa công khai của người gửi.
     * @return {@code true} nếu chữ ký hợp lệ, ngược lại {@code false}.
     */
    public static boolean verify(
            String data,
            String signatureBase64,
            PublicKey publicKey
    ) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = Base64.getDecoder().decode(signatureBase64);
            return signature.verify(signBytes);
        } catch (Exception e) {
            return false;
        }
    }
}
