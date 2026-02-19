package com.mycompany._thstudy.transaction.command.application.dto.request;

import com.mycompany._thstudy.category.command.domain.aggregate.CategoryType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TransactionCreateRequest {

    @NotNull
    private Long categoryId;

    @NotNull
    private CategoryType type;

    @NotNull
    @Min(1)
    private Long amount;

    @Size(max = 255)
    private String description;

    @NotNull
    private LocalDate transactionDate;
}
