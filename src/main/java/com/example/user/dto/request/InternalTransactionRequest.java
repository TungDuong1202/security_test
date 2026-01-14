package com.example.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) đại diện cho gói tin giao dịch bảo mật, đã được MÃ HÓA.
 * <p>
 * Class này được thiết kế cho việc <b>giao tiếp nội bộ giữa các service</b> (Internal Communication - ví dụ qua Kafka hoặc Feign Client).
 * Khác với {@link TransactionRequest} chứa dữ liệu thô, tất cả các trường trong class này đều là
 * <b>Văn bản mã hóa RSA (RSA Cipher Text)</b> dạng Base64.
 * </p>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalTransactionRequest {
    @NotBlank(message = "{transaction.id.required}")
    private String encryptedTransactionId;

    @NotBlank(message = "{transaction.source.required}")
    private String encryptedAccount;

    @NotBlank(message = "{transaction.debt.required}")
    private String encryptedInDebt;

    @NotBlank(message = "{transaction.have.required}")
    private String encryptedHave;

    @NotBlank(message = "{transaction.time.required}")
    private String encryptedTime;

    @Override
    public String toString() {
        return "SecureTransactionRequest[MASKED DATA]";
    }
}
