package com.mycompany._thstudy.transaction.command.application.dto.response;

import com.mycompany._thstudy.category.command.domain.aggregate.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@Builder
public class TransactionCommandResponse {

    private Long id;
    private Long accountId;
    private String accountName;
    private CategoryType type;
    private Long categoryId;
    private String categoryName;
    private Long amount;
    private String description;
    private LocalDate transactionDate;
}
