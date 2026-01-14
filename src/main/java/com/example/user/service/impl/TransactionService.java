package com.example.user.service.impl;

import com.example.user.dto.request.InternalTransactionRequest;
import com.example.user.dto.request.TransactionRequest;
import com.example.user.dto.response.TransactionResponse;
import com.example.user.dto.response.UserResponse;
import com.example.user.entity.TransactionHistory;
import com.example.user.entity.User;
import com.example.user.entity.UserProfile;
import com.example.user.exception.ConflictException;
import com.example.user.exception.NotFoundException;
import com.example.user.mapper.TransactionMapper;
import com.example.user.repository.ITransactionHistoryRepository;
import com.example.user.service.ITransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Class triển khai logic nghiệp vụ cho giao dịch tài chính (Transaction Service).
 * <p>
 * Class này chịu trách nhiệm:
 * <ul>
 * <li>Quản lý luồng tạo giao dịch theo nguyên lý kế toán kép (Double Entry).</li>
 * <li>Đảm bảo tính nhất quán dữ liệu (Atomicity) bằng @Transactional.</li>
 * <li>Truy vấn lịch sử và chuyển đổi dữ liệu mã hóa cho các internal services.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {
    private final ITransactionHistoryRepository repository;
    private final TransactionMapper transactionMapper;

    /**
     * Tạo và lưu trữ một giao dịch mới vào cơ sở dữ liệu.
     * <p>
     * Logic nghiệp vụ:
     * 1. Kiểm tra tính duy nhất của {@code transactionId} để tránh trùng lặp (Idempotency).
     * 2. Tạo bản ghi <b>DEBIT</b> (Ghi Nợ): Account nguồn bị trừ tiền (InDebt = Amount).
     * 3. Tạo bản ghi <b>CREDIT</b> (Ghi Có): Account đích được cộng tiền (Have = Amount).
     * 4. Dữ liệu Account sẽ được tự động mã hóa AES bởi JPA Converter trước khi lưu.
     * </p>
     *
     * @param request Đối tượng chứa thông tin giao dịch đầu vào.
     * @throws ConflictException Nếu {@code transactionId} đã tồn tại trong hệ thống.
     */
    @Override
    @Transactional
    public void createTransaction(TransactionRequest request) {
        List<TransactionHistory> transactionHistories = repository.findByTransactionId(request.getTransactionId());
        if (!transactionHistories.isEmpty()) {
            throw new ConflictException("Transaction ID already exists");
        }
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

    /**
     * Lấy chi tiết giao dịch dựa trên Transaction ID.
     *
     * @param transactionId Mã giao dịch cần tìm.
     * @return Danh sách {@link TransactionResponse} (bao gồm cả dòng Nợ và dòng Có).
     * @throws NotFoundException Nếu không tìm thấy giao dịch nào với ID cung cấp.
     */
    @Override
    public List<TransactionResponse> getTransactionByTransactionId(String transactionId) {
        List<TransactionHistory> transactionHistories = repository.findByTransactionId(transactionId);
        if (transactionHistories.isEmpty()) {
            throw new NotFoundException("Not found transaction ID");
        }
        return transactionHistories.stream().map(this::mapToResponse).toList();
    }

    /**
     * Chuyển đổi yêu cầu giao dịch thô thành các gói tin nội bộ được mã hóa.
     * <p>
     * Phương thức này sử dụng {@link TransactionMapper} để mã hóa RSA các trường nhạy cảm
     * (Account, Amount, Time) trước khi gửi sang các Service khác.
     * </p>
     *
     * @param request Dữ liệu giao dịch gốc.
     * @return Danh sách chứa 2 gói tin {@link InternalTransactionRequest} (1 cho bên Nợ, 1 cho bên Có).
     */
    @Override
    public List<InternalTransactionRequest> getInternalTransactionRequest(TransactionRequest request) {

        InternalTransactionRequest debitRequest = transactionMapper.toEncryptedRequest(
                request.getTransactionId(),
                request.getSourceAccount(),
                request.getTime().toString(),
                request.getAmount(),
                BigDecimal.ZERO
        );

        InternalTransactionRequest creditRequest = transactionMapper.toEncryptedRequest(
                request.getTransactionId(),
                request.getDestAccount(),
                request.getTime().toString(),
                BigDecimal.ZERO,
                request.getAmount()
        );

        return List.of(debitRequest, creditRequest);
    }

    private TransactionResponse mapToResponse(TransactionHistory transactionHistory) {
        return TransactionResponse.builder()
                .transactionId(transactionHistory.getTransactionId())
                .account(transactionHistory.getAccount())
                .inDebt(transactionHistory.getInDebt())
                .have(transactionHistory.getHave())
                .time(transactionHistory.getTime())
                .build();
    }
}
