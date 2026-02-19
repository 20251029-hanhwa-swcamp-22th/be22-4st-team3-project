package com.mycompany._thstudy.transaction.query.service;

import com.mycompany._thstudy.transaction.query.dto.request.TransactionSearchRequest;
import com.mycompany._thstudy.transaction.query.dto.response.CategoryRawSummary;
import com.mycompany._thstudy.transaction.query.dto.response.DailySummaryResponse;
import com.mycompany._thstudy.transaction.query.dto.response.MonthlySummaryResponse;
import com.mycompany._thstudy.transaction.query.dto.response.TransactionListResponse;
import com.mycompany._thstudy.transaction.query.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionQueryService {

  private final TransactionMapper transactionMapper;

  public List<TransactionListResponse> getTransactions(String userEmail, TransactionSearchRequest req) {
    if (req.getStartDate() == null) {
      req.setStartDate(LocalDate.now().withDayOfMonth(1));
    }
    if (req.getEndDate() == null) {
      req.setEndDate(LocalDate.now());
    }
    return transactionMapper.findByFilter(userEmail, req);
  }

  public MonthlySummaryResponse getMonthlySummary(String userEmail, int year, int month) {

    List<CategoryRawSummary> rawList =
        transactionMapper.findMonthlySummary(userEmail, year, month);

    // INCOME / EXPENSE 분리
    List<CategoryRawSummary> incomeRaw = rawList.stream()
        .filter(r -> r.getType().equals("INCOME")).toList();
    List<CategoryRawSummary> expenseRaw = rawList.stream()
        .filter(r -> r.getType().equals("EXPENSE")).toList();

    // 총합 계산
    long totalIncome  = incomeRaw.stream().mapToLong(CategoryRawSummary::getAmount).sum();
    long totalExpense = expenseRaw.stream().mapToLong(CategoryRawSummary::getAmount).sum();

    // CategorySummary 변환 (비율 포함)
    List<MonthlySummaryResponse.CategorySummary> incomeSummary = incomeRaw.stream()
        .map(r -> new MonthlySummaryResponse.CategorySummary(
            r.getCategoryName(), r.getAmount(),
            totalIncome == 0 ? 0 : (double) r.getAmount() / totalIncome * 100
        )).toList();

    List<MonthlySummaryResponse.CategorySummary> expenseSummary = expenseRaw.stream()
        .map(r -> new MonthlySummaryResponse.CategorySummary(
            r.getCategoryName(), r.getAmount(),
            totalExpense == 0 ? 0 : (double) r.getAmount() / totalExpense * 100
        )).toList();

    return new MonthlySummaryResponse(
        year, month,
        totalIncome, totalExpense,
        totalIncome - totalExpense,
        incomeSummary, expenseSummary
    );
  }

  public List<DailySummaryResponse> getDailySummary(String userEmail, int year, int month) {
    return transactionMapper.findDailySummary(userEmail, year, month);
  }

  public List<TransactionListResponse> getRecentTransactions(String userEmail) {
    return transactionMapper.findRecentByUserEmail(userEmail);
  }

}
