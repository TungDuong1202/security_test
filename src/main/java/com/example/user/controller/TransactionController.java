package com.example.user.controller;


import com.example.user.dto.request.InternalTransactionRequest;
import com.example.user.dto.request.TransactionDecryptedDTO;
import com.example.user.dto.request.TransactionRequest;
import com.example.user.dto.response.ApiResponseEntity;
import com.example.user.dto.response.ApiResponseFactory;

import com.example.user.dto.response.TransactionResponse;
import com.example.user.mapper.TransactionMapper;
import com.example.user.service.ITransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.security.PublicKey;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class TransactionController {
    private final ITransactionService transactionService;
    private final TransactionMapper mapper;

    @Operation(
            summary = "Create a new transaction",
            description = "Creates 2 accounting entries (Debit/Credit), encrypts Account data, and persists the transaction to the Database."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (Validation Failed)"),
            @ApiResponse(responseCode = "409", description = "Transaction ID already exists (Conflict)")
    })
    @PostMapping
    public ApiResponseEntity<?> createTransaction(@Valid @RequestBody TransactionRequest request) {
        transactionService.createTransaction(request);
        return ApiResponseFactory.created(null);
    }

    @Operation(
            summary = "Get transaction details",
            description = "Retrieves a list of accounting entries (Debit & Credit) based on the Transaction ID. Account data is automatically decrypted."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found with the provided ID")
    })
    @GetMapping("/{transactionId}")
    public ApiResponseEntity<List<TransactionResponse>> getTransactionById(
            @PathVariable String transactionId) {

        return ApiResponseFactory.success(transactionService.getTransactionByTransactionId(transactionId));
    }

    @Operation(
            summary = "[TEST] Simulate Encryption (Raw -> Encrypted)",
            description = "Utility API to verify Mapper logic: Converts raw transaction data into encrypted internal packets (RSA)."
    )
    @GetMapping("/test-encrypt")
    public ApiResponseEntity<List<InternalTransactionRequest>> getInternalTransactionRequest(@Valid @RequestBody TransactionRequest request) {

        return ApiResponseFactory.success(transactionService.getInternalTransactionRequest(request));
    }

    @Operation(
            summary = "[TEST] Simulate Decryption (Encrypted -> Raw)",
            description = "Utility API to verify Mapper logic: Decrypts internal packets back into readable raw data."
    )
    @GetMapping("/test-decrypt")
    public ApiResponseEntity<TransactionDecryptedDTO> getDecryptInternalTransactionRequest(@Valid @RequestBody InternalTransactionRequest request) {
        return ApiResponseFactory.success(mapper.toDecryptedData(request));
    }

    @Operation(
            summary = "[TEST] Verify Log Masking",
            description = "Intentionally throws an exception to trigger logging. Use this to verify if TransactionID and Account are masked with '?' in the server logs."
    )
    @ApiResponse(responseCode = "500", description = "Internal Server Error (Expected for log testing)")
    @GetMapping("/test-log")
    public void testLog() {
        log.error(
                "Lỗi giao dịch: TransactionID=TX-12345, Account=0123-456-789, InDebt=1000000,\n" +
                        "Have=0, Time=2024-10-12T10:15:30"
        );
        throw new RuntimeException("Test exception");
    }
}
