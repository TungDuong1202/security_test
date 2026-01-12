package com.example.user.controller;

import com.example.user.dto.request.SecureTransactionRequest;
import com.example.user.dto.request.TransactionRequest;
import com.example.user.dto.response.ApiResponseEntity;
import com.example.user.dto.response.ApiResponseFactory;
import com.example.user.mapper.TransactionMapper;
import com.example.user.service.ITransactionService;
import com.example.user.service.impl.TransactionService;
import com.example.user.utils.RsaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final ITransactionService transactionService;
    private final TransactionMapper transactionMapper;
    private final PublicKey serverPublicKey;

    @PostMapping
    public ApiResponseEntity<String> createTransaction(@RequestBody SecureTransactionRequest request) {

        log.info("Received transaction request: {}", request);

        // 1. Giải mã và Validate (Lỗi sẽ ném ra Exception và GlobalHandler bắt)
        TransactionRequest plainDto = transactionMapper.toPlainDto(request);

        // 2. Gọi Service xử lý
        transactionService.processTransaction(plainDto);

        return ApiResponseFactory.created(null);
    }

    /**
     * API Giả lập Client (Chỉ dùng để test).
     * Nhập JSON thường -> Trả về JSON đã mã hóa RSA để copy vào API chính.
     */
    @PostMapping("/encrypt-payload")
    public SecureTransactionRequest generateEncryptedPayload(@RequestBody TransactionRequest rawRequest) {
        SecureTransactionRequest secureReq = new SecureTransactionRequest();

        // Giả lập Client dùng Public Key của Server để mã hóa
        secureReq.setEncryptedTransactionId(RsaUtil.encrypt(rawRequest.getTransactionId(), serverPublicKey));
        secureReq.setEncryptedSourceAccount(RsaUtil.encrypt(rawRequest.getSourceAccount(), serverPublicKey));
        secureReq.setEncryptedDestAccount(RsaUtil.encrypt(rawRequest.getDestAccount(), serverPublicKey));
        secureReq.setEncryptedAmount(RsaUtil.encrypt(rawRequest.getAmount().toString(), serverPublicKey));
        secureReq.setEncryptedTime(RsaUtil.encrypt(rawRequest.getTime().toString(), serverPublicKey));

        return secureReq;
    }
}
