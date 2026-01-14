package com.example.user.service;

import com.example.user.dto.request.InternalTransactionRequest;
import com.example.user.dto.request.TransactionRequest;
import com.example.user.dto.response.TransactionResponse;

import java.util.List;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến Giao dịch tài chính (Transaction).
 * <p>
 * Service này chịu trách nhiệm:
 * <ul>
 * <li>Thực hiện nguyên lý kế toán kép (Double Entry Bookkeeping): Lưu 2 bản ghi Nợ/Có cho mỗi giao dịch.</li>
 * <li>Truy vấn lịch sử giao dịch.</li>
 * <li>Chuẩn bị và mã hóa dữ liệu để giao tiếp với các Internal Service khác.</li>
 * </ul>
 */
public interface ITransactionService {
    /**
     * Xử lý và lưu trữ một giao dịch chuyển tiền mới vào hệ thống.
     * <p>
     * Phương thức này sẽ:
     * 1. Validate logic nghiệp vụ (số dư, trạng thái tài khoản...).
     * 2. Tạo 2 bản ghi {@code TransactionHistory}:
     * - Một bản ghi <b>DEBIT</b> (Ghi Nợ) cho người gửi.
     * - Một bản ghi <b>CREDIT</b> (Ghi Có) cho người nhận.
     * 3. Dữ liệu nhạy cảm (Account) sẽ được mã hóa tự động trước khi lưu xuống DB.
     * </p>
     *
     * @param request Đối tượng chứa thông tin giao dịch thô (Transaction ID, Account nguồn/đích, Số tiền...).
     */
    void createTransaction(TransactionRequest request);

    /**
     * Truy vấn chi tiết giao dịch dựa trên Mã giao dịch (Transaction ID).
     *
     * @param transactionId Mã định danh duy nhất của giao dịch cần tìm.
     * @return Danh sách {@link TransactionResponse}.
     */
    List<TransactionResponse> getTransactionByTransactionId(String transactionId);

    /**
     * Chuyển đổi yêu cầu giao dịch thô (Raw) thành các gói tin nội bộ đã được MÃ HÓA.
     * <p>
     * Phương thức này dùng để chuẩn bị dữ liệu an toàn trước khi gửi sang các Service khác
     * (thông qua Kafka hoặc FeignClient).
     * </p>
     * <ul>
     * <li>Tách yêu cầu thành 2 gói tin riêng biệt: 1 gói cho bên Nợ, 1 gói cho bên Có.</li>
     * <li>Mã hóa RSA các trường nhạy cảm: Account, Amount (InDebt/Have), Time.</li>
     * </ul>
     *
     * @param request Dữ liệu giao dịch đầu vào (Plain text).
     * @return Danh sách chứa 2 đối tượng {@link InternalTransactionRequest} đã được mã hóa.
     */
    List<InternalTransactionRequest> getInternalTransactionRequest(TransactionRequest request);
}
