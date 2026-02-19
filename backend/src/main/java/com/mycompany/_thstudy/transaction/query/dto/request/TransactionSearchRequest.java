package com.mycompany._thstudy.transaction.query.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TransactionSearchRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private String type;        // INCOME / EXPENSE (null 이면 전체)
    private Long categoryId;    // null 이면 전체
    private String keyword;     // description LIKE 검색
    private Long minAmount;     // null 이면 하한 없음
    private Long maxAmount;     // null 이면 상한 없음
}
