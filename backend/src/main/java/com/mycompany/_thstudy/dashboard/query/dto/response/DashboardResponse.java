package com.mycompany._thstudy.dashboard.query.dto.response;

import com.mycompany._thstudy.transaction.query.dto.response.TransactionListResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

  private Long totalBalance;
  private Long monthlyIncome;
  private Long monthlyExpense;
  private List<TransactionListResponse> recentTransactions;
}

