package com.mycompany._thstudy.transaction.command.domain.repository;

import com.mycompany._thstudy.transaction.command.domain.aggregate.Transaction;

import java.util.Optional;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Optional<Transaction> findById(Long id);

    void delete(Transaction transaction);

    boolean existsByCategoryId(Long categoryId);
}
