package com.example.user.service;

import com.example.user.dto.request.SecureTransactionRequest;
import com.example.user.dto.request.TransactionRequest;

public interface ITransactionService {
    void processTransaction(TransactionRequest request);
}
