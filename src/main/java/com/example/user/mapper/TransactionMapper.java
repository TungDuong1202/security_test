package com.example.user.mapper;

import com.example.user.dto.request.InternalTransactionRequest;
import com.example.user.dto.request.TransactionDecryptedDTO;
import com.example.user.utils.RsaUtil;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;


import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Mapper chịu trách nhiệm chuyển đổi và MÃ HÓA / GIẢI MÃ dữ liệu giao dịch.
 * <p>
 * Class này đóng vai trò là lớp bảo mật trung gian (Security Layer) giữa dữ liệu thô (Raw Data)
 * và dữ liệu nội bộ (Internal Data) khi giao tiếp giữa các services.
 * </p>
 * <ul>
 * <li>Sử dụng {@link org.mapstruct} để mapping tự động.</li>
 * <li>Sử dụng {@link RsaUtil} kết hợp với RSA Key Pair để bảo vệ dữ liệu.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public abstract class TransactionMapper {

    /**
     * Public Key dùng để MÃ HÓA dữ liệu (chiều gửi đi).
     */
    @Autowired
    protected PublicKey publicKey;
    /**
     * Private Key dùng để GIẢI MÃ dữ liệu (chiều nhận về).
     */
    @Autowired
    protected PrivateKey privateKey;

    /**
     * Helper method: Mã hóa một chuỗi văn bản bằng RSA Public Key.
     *
     * @param value Chuỗi gốc (Plain text).
     * @return Chuỗi đã mã hóa (Cipher text Base64), hoặc null nếu input null.
     */
    @Named("rsaEncrypt")
    public String rsaEncrypt(String value) {
        if (Objects.isNull(value)) return null;
        return RsaUtil.encrypt(value, publicKey);
    }

    /**
     * Helper method: Mã hóa số BigDecimal.
     * <p>Chuyển số thành String rồi mới mã hóa.</p>
     *
     * @param value Giá trị số tiền.
     * @return Chuỗi mã hóa của số tiền.
     */
    @Named("rsaEncrypt")
    public String rsaEncrypt(BigDecimal value) {
        if (Objects.isNull(value)) return null;
        return RsaUtil.encrypt(value.toPlainString(), publicKey);
    }

    /**
     * Chuyển đổi các trường thông tin rời rạc thành một gói tin giao dịch nội bộ ĐÃ MÃ HÓA.
     * <p>
     * Phương thức này được gọi khi Service A muốn gửi dữ liệu sang Service B.
     * Tất cả các trường quan trọng (Account, Amount, Time...) đều sẽ bị mã hóa.
     * </p>
     *
     * @param transactionId Mã giao dịch.
     * @param account       Số tài khoản.
     * @param time          Thời gian giao dịch (String format).
     * @param inDebt        Số tiền ghi Nợ.
     * @param have          Số tiền ghi Có.
     * @return {@link InternalTransactionRequest} Object chứa toàn bộ dữ liệu đã được mã hóa RSA.
     */
    @Mapping(target = "encryptedTransactionId", source = "transactionId", qualifiedByName = "rsaEncrypt")
    @Mapping(target = "encryptedAccount", source = "account", qualifiedByName = "rsaEncrypt")
    @Mapping(target = "encryptedTime", source = "time", qualifiedByName = "rsaEncrypt")
    @Mapping(target = "encryptedInDebt", source = "inDebt", qualifiedByName = "rsaEncrypt")
    @Mapping(target = "encryptedHave", source = "have", qualifiedByName = "rsaEncrypt")
    public abstract InternalTransactionRequest toEncryptedRequest(
            String transactionId,
            String account,
            String time,
            BigDecimal inDebt,
            BigDecimal have
    );

    /**
     * Helper method: Giải mã chuỗi đã mã hóa bằng RSA Private Key.
     *
     * @param encryptedValue Chuỗi mã hóa (Cipher text).
     * @return Chuỗi gốc (Plain text).
     */
    @Named("rsaDecryptString")
    public String rsaDecryptString(String encryptedValue) {
        if (Objects.isNull(encryptedValue)) return null;
        return RsaUtil.decrypt(encryptedValue, privateKey);
    }
    /**
     * Helper method: Giải mã chuỗi về dạng số BigDecimal.
     *
     * @param encryptedValue Chuỗi mã hóa của số tiền.
     * @return Đối tượng {@link BigDecimal}.
     */
    @Named("rsaDecryptBigDecimal")
    public BigDecimal rsaDecryptBigDecimal(String encryptedValue) {
        if (Objects.isNull(encryptedValue)) return null;
        String plainText = RsaUtil.decrypt(encryptedValue, privateKey);
        return new BigDecimal(plainText);
    }
    /**
     * Helper method: Giải mã chuỗi về dạng thời gian LocalDateTime.
     * <p>Giả định chuỗi gốc tuân thủ định dạng ISO-8601 mặc định.</p>
     *
     * @param encryptedValue Chuỗi mã hóa của thời gian.
     * @return Đối tượng {@link LocalDateTime}.
     */
    @Named("rsaDecryptTime")
    public LocalDateTime rsaDecryptTime(String encryptedValue) {
        if (Objects.isNull(encryptedValue)) return null;
        String plainText = RsaUtil.decrypt(encryptedValue, privateKey);
        return LocalDateTime.parse(plainText);
    }

    /**
     * Chuyển đổi gói tin nội bộ (đang mã hóa) thành DTO dữ liệu thực (đã giải mã).
     * <p>
     * Phương thức này được gọi khi Service B nhận được gói tin từ Service A và cần đọc dữ liệu.
     * </p>
     *
     * @param request Gói tin {@link InternalTransactionRequest} chứa dữ liệu mã hóa.
     * @return {@link TransactionDecryptedDTO} Object chứa dữ liệu gốc (Plain text/Number/Time) để xử lý logic.
     */
    @Mapping(target = "transactionId", source = "encryptedTransactionId", qualifiedByName = "rsaDecryptString")
    @Mapping(target = "account", source = "encryptedAccount", qualifiedByName = "rsaDecryptString")
    @Mapping(target = "time", source = "encryptedTime", qualifiedByName = "rsaDecryptTime")
    @Mapping(target = "inDebt", source = "encryptedInDebt", qualifiedByName = "rsaDecryptBigDecimal")
    @Mapping(target = "have", source = "encryptedHave", qualifiedByName = "rsaDecryptBigDecimal")
    public abstract TransactionDecryptedDTO toDecryptedData(InternalTransactionRequest request);
}
