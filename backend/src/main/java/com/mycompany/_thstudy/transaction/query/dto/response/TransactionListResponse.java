package com.mycompany._thstudy.transaction.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListResponse {

    private Long id;
    private Long accountId;
    private String accountName;
    private String type;
    private Long categoryId;
    private String categoryName;
    private Long amount;
    private String description;
    private LocalDate transactionDate;
}
