package com.example.user.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDecryptedDTO {
    private String transactionId;
    private String account;

    private LocalDateTime time;

    private BigDecimal inDebt;
    private BigDecimal have;
}