package com.example.user.service.impl;

import com.example.user.repository.ITransactionHistoryRepository;
import com.example.user.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {
    private final ITransactionHistoryRepository repository;
}
