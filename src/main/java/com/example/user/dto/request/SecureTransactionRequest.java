package com.example.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SecureTransactionRequest {
    @NotBlank(message = "Transaction ID không được để trống")
    private String encryptedTransactionId;

    @NotBlank(message = "Tài khoản nguồn là bắt buộc")
    private String encryptedSourceAccount;

    @NotBlank(message = "Tài khoản đích là bắt buộc")
    private String encryptedDestAccount;

    @NotNull(message = "Số tiền là bắt buộc")
    private String encryptedAmount;

    @NotNull(message = "Thời gian giao dịch là bắt buộc")
    private String encryptedTime;

    @Override
    public String toString() {
        return "SecureTransactionRequest[MASKED DATA]";
    }
}
