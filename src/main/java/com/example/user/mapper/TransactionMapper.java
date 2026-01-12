package com.example.user.mapper;

import com.example.user.dto.request.SecureTransactionRequest;
import com.example.user.dto.request.TransactionRequest;
import com.example.user.utils.RsaUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class TransactionMapper {
    private final PrivateKey privateKey;
    private final Validator validator;

    public TransactionRequest toPlainDto(SecureTransactionRequest secureRequest) {

        String transId = RsaUtil.decrypt(secureRequest.getEncryptedTransactionId(), privateKey);
        String source = RsaUtil.decrypt(secureRequest.getEncryptedSourceAccount(), privateKey);
        String dest = RsaUtil.decrypt(secureRequest.getEncryptedDestAccount(), privateKey);
        String amountStr = RsaUtil.decrypt(secureRequest.getEncryptedAmount(), privateKey);
        String timeStr = RsaUtil.decrypt(secureRequest.getEncryptedTime(), privateKey);

        TransactionRequest dto =  TransactionRequest.builder()
                .transactionId(transId)
                .sourceAccount(source)
                .destAccount(dest)
                .amount(new BigDecimal(amountStr))
                .time(LocalDateTime.parse(timeStr))
                .build();
        validateDto(dto);
        return dto;
    }
    private void validateDto(TransactionRequest dto) {
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
