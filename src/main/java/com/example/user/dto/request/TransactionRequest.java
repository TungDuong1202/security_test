package com.example.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionRequest {
    @NotBlank(message = "Transaction ID không được để trống")
    private String transactionId;

    @NotBlank(message = "Tài khoản nguồn là bắt buộc")
    @Pattern(regexp = "\\d{10,13}", message = "Tài khoản nguồn phải là số từ 10-13 ký tự")
    private String sourceAccount;

    @NotBlank(message = "Tài khoản đích là bắt buộc")
    @Pattern(regexp = "\\d{10,13}", message = "Tài khoản đích phải là số từ 10-13 ký tự")
    private String destAccount;

    @NotNull(message = "Số tiền là bắt buộc")
    @Positive(message = "Số tiền phải lớn hơn 0")
    @Min(value = 10000, message = "Giao dịch tối thiểu 10,000đ")
    private BigDecimal amount;

    @NotNull(message = "Thời gian giao dịch là bắt buộc")
    private LocalDateTime time;

    @Override
    public String toString() {
        return "TransactionRequest{ TransactionId=?, Source=?, Dest=?, Amount=?, Time=? }";
    }
}
