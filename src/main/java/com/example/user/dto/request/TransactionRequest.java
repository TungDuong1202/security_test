package com.example.user.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) đại diện cho yêu cầu tạo giao dịch mới từ phía Client.
 * <p>
 * Class này chứa các thông tin đầu vào (Input) cần thiết để thực hiện một giao dịch chuyển tiền.
 * Các trường dữ liệu đều được kiểm tra tính hợp lệ (Validate) chặt chẽ trước khi đi vào xử lý nghiệp vụ.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {
    @NotBlank(message = "{transaction.id.required}")
    private String transactionId;

    @NotBlank(message = "{transaction.source.required}")
    @Pattern(regexp = "\\d{10,13}", message = "{transaction.source.invalid}")
    private String sourceAccount;

    @NotBlank(message = "{transaction.dest.required}")
    @Pattern(regexp = "\\d{10,13}", message = "{transaction.dest.invalid}")
    private String destAccount;

    @NotNull(message = "{transaction.amount.required}")
    @Positive(message = "{transaction.amount.positive}")
    @Min(value = 10000, message = "{transaction.amount.min}")
    private BigDecimal amount;

    @NotNull(message = "{transaction.time.required}")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime time;

    @Override
    public String toString() {
        return "TransactionRequest{ TransactionId=?, sourceAccount=?, destAccount=?, Amount=?, Time=? }";
    }
}
