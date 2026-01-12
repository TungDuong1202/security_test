package com.example.user.dto.request;

import java.math.BigDecimal;

public class TransferRequest {
    private String transactionId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
}
