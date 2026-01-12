package com.example.user.entity;

import com.example.user.converter.AccountEncryptConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    @Convert(converter = AccountEncryptConverter.class)
    private String account;

    private BigDecimal inDebt;
    private BigDecimal have;

    private LocalDateTime time;
}
