package com.mycompany._thstudy.transaction.command.application.service;

import com.mycompany._thstudy.category.command.domain.aggregate.Category;
import com.mycompany._thstudy.category.command.domain.repository.CategoryRepository;
import com.mycompany._thstudy.exception.BusinessException;
import com.mycompany._thstudy.exception.ErrorCode;
import com.mycompany._thstudy.transaction.command.application.dto.request.TransactionCreateRequest;
import com.mycompany._thstudy.transaction.command.application.dto.request.TransactionUpdateRequest;
import com.mycompany._thstudy.transaction.command.application.dto.response.TransactionCommandResponse;
import com.mycompany._thstudy.transaction.command.domain.aggregate.Transaction;
import com.mycompany._thstudy.transaction.command.domain.repository.TransactionRepository;
import com.mycompany._thstudy.user.command.domain.aggregate.User;
import com.mycompany._thstudy.user.command.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionCommandService {

  private final TransactionRepository transactionRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  public TransactionCommandResponse createTransaction(String userEmail, TransactionCreateRequest request) {
    // TODO: 구현
    // 1. userRepository.findByEmail(userEmail) → USER_NOT_FOUND
    User user = userRepository.findByEmail(userEmail).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 2. categoryRepository.findById(request.getCategoryId()) → CATEGORY_NOT_FOUND
    Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(()-> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

    if(!category.getType().equals(request.getType())){
      throw new BusinessException(ErrorCode.CATEGORY_TYPE_MISMATCH);
    }

    // 3. category.getUser().getEmail().equals(userEmail) 확인 → ACCESS_DENIED
    if(!category.getUser().getEmail().equals(userEmail)){
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    // 거래 금액이 0이하인 경우 예외 처리
    if (request.getAmount() <=0){
      throw new BusinessException(ErrorCode.NEGATIVE_AMOUNT);
    }

    // 4. Transaction.builder()...build() → save()
    Transaction transaction = Transaction.builder()
        .user(user)
        .category(category)
        .type(request.getType())
        .amount(request.getAmount())
        .description(request.getDescription())
        .transactionDate(request.getTransactionDate())
        .build();

    Transaction savedTransaction = transactionRepository.save(transaction);
    // 5. TransactionCommandResponse 반환
    return TransactionCommandResponse.builder()
        .id(savedTransaction.getId())
        .type(savedTransaction.getType())
        .categoryId(savedTransaction.getCategory().getId())
        .categoryName(savedTransaction.getCategory().getName())
        .amount(savedTransaction.getAmount())
        .description(savedTransaction.getDescription())
        .transactionDate(savedTransaction.getTransactionDate())
        .build();
  }

  public TransactionCommandResponse updateTransaction(String userEmail, Long transactionId, TransactionUpdateRequest request) {
    // TODO: 구현
    // 1. transactionRepository.findById(transactionId) → TRANSACTION_NOT_FOUND
    Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(
        ()-> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND)
    );
    // 2. transaction.getUser().getEmail().equals(userEmail) 확인 → ACCESS_DENIED
    if(!transaction.getUser().getEmail().equals(userEmail)){
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    // 3. categoryRepository.findById(request.getCategoryId()) → CATEGORY_NOT_FOUND
    Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(
        ()-> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND)
    );

    if (!category.getUser().getEmail().equals(userEmail)){
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    if(!category.getType().equals(request.getType())){
      throw new BusinessException(ErrorCode.CATEGORY_TYPE_MISMATCH);
    }

    // 4. category.getUser().getEmail().equals(userEmail) 확인 → ACCESS_DENIED
    if(!category.getUser().getEmail().equals(userEmail)){
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    // 5. transaction.update(...)
    transaction.update(
        category,
        request.getType(),
        request.getAmount(),
        request.getDescription(),
        request.getTransactionDate()
    );
    // 6. TransactionCommandResponse 반환
    return TransactionCommandResponse.builder()
        .id(transaction.getId())
        .type(transaction.getType())
        .categoryId(transaction.getCategory().getId())
        .categoryName(transaction.getCategory().getName())
        .amount(transaction.getAmount())
        .description(transaction.getDescription())
        .transactionDate(transaction.getTransactionDate())
        .build();
  }

  public void deleteTransaction(String userEmail, Long transactionId) {
    // TODO: 구현
    // 1. transactionRepository.findById(transactionId) → TRANSACTION_NOT_FOUND
    Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(
        ()-> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND)
    );
    // 2. transaction.getUser().getEmail().equals(userEmail) 확인 → ACCESS_DENIED
    if(!transaction.getUser().getEmail().equals(userEmail)){
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
    // 3. transactionRepository.delete(transaction)
    transactionRepository.delete(transaction);
  }
}
