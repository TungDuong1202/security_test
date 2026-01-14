package com.example.user.converter;

import com.example.user.utils.AesUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.util.Objects;

/**
 * JPA Attribute Converter dùng để tự động MÃ HÓA và GIẢI MÃ dữ liệu số tài khoản (Account).
 * <p>
 * Class này hiện thực cơ chế <b>Encryption at Rest</b> (Bảo mật dữ liệu khi lưu trữ).
 * Nó hoạt động như một lớp trung gian trong suốt (Transparent Middleware) giữa Entity và Database.
 * </p>
 *
 * <b>Cơ chế hoạt động:</b>
 * <ul>
 * <li><b>Khi Lưu (Write):</b> Chuyển đổi số tài khoản thực (Plain Text) -> Chuỗi mã hóa (Cipher Text) trước khi INSERT/UPDATE vào DB.</li>
 * <li><b>Khi Đọc (Read):</b> Chuyển đổi chuỗi mã hóa từ DB -> Số tài khoản thực để hiển thị trên Entity.</li>
 * </ul>
 * @see AttributeConverter
 * @see AesUtil
 */
@Converter
@RequiredArgsConstructor
public class AccountEncryptConverter implements AttributeConverter<String, String> {
    private final SecretKey secretKey;

    /**
     * Chuyển đổi dữ liệu từ Entity thành dữ liệu lưu xuống Database (Mã hóa).
     * <p>
     * Phương thức này được Hibernate/JPA gọi tự động trước khi thực hiện lệnh INSERT hoặc UPDATE.
     * </p>
     *
     * @param attribute Giá trị thuộc tính trong Entity (Số tài khoản thật - Plain Text).
     * @return Chuỗi đã được mã hóa (Cipher Text) để lưu vào cột Database.
     * Trả về {@code null} nếu giá trị đầu vào là null.
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return Objects.isNull(attribute) ? null : AesUtil.encrypt(attribute, secretKey);
    }

    /**
     * Chuyển đổi dữ liệu từ Database thành dữ liệu cho Entity (Giải mã).
     * <p>
     * Phương thức này được Hibernate/JPA gọi tự động sau khi thực hiện lệnh SELECT.
     * </p>
     *
     * @param dbData Giá trị lấy từ cột Database (Chuỗi đã mã hóa - Cipher Text).
     * @return Số tài khoản thực (Plain Text) để map vào Entity.
     * Trả về {@code null} nếu dữ liệu trong DB là null.
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        return Objects.isNull(dbData) ? null : AesUtil.decrypt(dbData, secretKey);
    }
}