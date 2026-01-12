package com.example.user.repository;

import com.example.user.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
}
