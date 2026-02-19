package com.mycompany._thstudy.dashboard.query.service;

import com.mycompany._thstudy.account.query.dto.response.AccountSummaryResponse;
import com.mycompany._thstudy.account.query.service.AccountQueryService;
import com.mycompany._thstudy.dashboard.query.dto.response.DashboardResponse;
import com.mycompany._thstudy.transaction.query.dto.response.MonthlySummaryResponse;
import com.mycompany._thstudy.transaction.query.dto.response.TransactionListResponse;
import com.mycompany._thstudy.transaction.query.service.TransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryService {

  private final AccountQueryService accountQueryService;
  private final TransactionQueryService transactionQueryService;

  public DashboardResponse getDashboard(String userEmail) {
    LocalDate today = LocalDate.now();

    AccountSummaryResponse accountSummary = accountQueryService.getAccountSummary(userEmail);
    MonthlySummaryResponse monthlySummary = transactionQueryService.getMonthlySummary(
        userEmail,
        today.getYear(),
        today.getMonthValue()
    );
    List<TransactionListResponse> recentTransactions = transactionQueryService.getRecentTransactions(userEmail);

    return new DashboardResponse(
        accountSummary.getTotalBalance(),
        monthlySummary.getTotalIncome(),
        monthlySummary.getTotalExpense(),
        recentTransactions
    );
  }
}

