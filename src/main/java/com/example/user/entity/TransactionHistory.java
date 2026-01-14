package com.example.user.entity;

import com.example.user.converter.AccountEncryptConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "transaction_id", nullable = false)
    @NotBlank(message = "Transaction ID cannot be blank")
    private String transactionId;

    @Column(name = "account", nullable = false)
    @NotBlank(message = "Account cannot be blank")
    @Pattern(regexp = "\\d{10,13}", message = "Account must be between 10 and 13 digits")
    @Convert(converter = AccountEncryptConverter.class)
    private String account;

    @Column(name = "in_debt", nullable = false)
    @NotNull(message = "InDebt amount cannot be null")
    @Min(value = 0, message = "InDebt cannot be negative")
    private BigDecimal inDebt;

    @Column(name = "have", nullable = false)
    @NotNull(message = "InDebt amount cannot be null")
    @Min(value = 0, message = "InDebt cannot be negative")
    private BigDecimal have;

    @Column(name = "time", nullable = false)
    @NotNull(message = "Transaction time cannot be null")
    private LocalDateTime time;

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
