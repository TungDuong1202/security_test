package com.example.user.service.impl;

import com.example.user.dto.request.SecureTransactionRequest;
import com.example.user.dto.request.TransactionRequest;
import com.example.user.entity.TransactionHistory;
import com.example.user.repository.ITransactionHistoryRepository;
import com.example.user.service.ITransactionService;
import com.example.user.utils.RsaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {
    private final ITransactionHistoryRepository repository;

    @Override
    public void processTransaction(TransactionRequest request) {

        TransactionHistory debit = TransactionHistory.builder()
                .transactionId(request.getTransactionId())
                .account(request.getSourceAccount())
                .inDebt(request.getAmount())
                .have(BigDecimal.ZERO)
                .time(request.getTime())
                .build();

        TransactionHistory credit = TransactionHistory.builder()
                .transactionId(request.getTransactionId())
                .account(request.getDestAccount())
                .inDebt(BigDecimal.ZERO)
                .have(request.getAmount())
                .time(request.getTime())
                .build();

        repository.save(debit);
        repository.save(credit);
    }
}
