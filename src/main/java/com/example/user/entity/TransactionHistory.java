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

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "account", nullable = false)
    @Convert(converter = AccountEncryptConverter.class)
    private String account;

    @Column(name = "in_debt")
    private BigDecimal inDebt;
    @Column(name = "have")
    private BigDecimal have;
    @Column(name = "time")
    private LocalDateTime time;

    // Override toString để che dấu log (Masking)
    @Override
    public String toString() {
        return "TransactionHistory{" +
                "id=" + id +
                ", transactionId='?', " +
                "account='?', " +
                "inDebt=?, " +
                "have=?, " +
                "time=?" +
                '}';
    }
}
