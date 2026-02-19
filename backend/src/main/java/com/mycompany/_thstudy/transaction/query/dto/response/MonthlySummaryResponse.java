package com.mycompany._thstudy.transaction.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// MonthlySummaryResponse.java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryResponse {
    private int year;
    private int month;
    private long totalIncome;   // 총 수입
    private long totalExpense;  // 총 지출
    private long balance;       // 잔액 (수입 - 지출)
    private List<CategorySummary> incomeSummary;   // 카테고리별 수입
    private List<CategorySummary> expenseSummary;  // 카테고리별 지출

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private String categoryName;
        private long amount;
        private double percentage; // 전체 대비 비율
    }
}
