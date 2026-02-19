package com.mycompany._thstudy.transaction.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// transaction/query/dto/response/CategoryRawSummary.java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRawSummary {
    private String categoryName;
    private String type;   // "INCOME" or "EXPENSE"
    private long amount;
}
