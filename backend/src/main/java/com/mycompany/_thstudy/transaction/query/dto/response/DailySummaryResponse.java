package com.mycompany._thstudy.transaction.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryResponse {
    private LocalDate transactionDate;
    private long totalIncome;
    private long totalExpense;
}
