package com.mycompany._thstudy.transaction.command.infrastructure.repository;

import com.mycompany._thstudy.transaction.command.domain.aggregate.Transaction;
import com.mycompany._thstudy.transaction.command.domain.repository.TransactionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTransactionRepository extends JpaRepository<Transaction, Long>, TransactionRepository {

    @Override
    boolean existsByCategoryId(Long categoryId);
}
