package com.mycompany._thstudy.transaction.query.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TransactionSearchRequest {

    private LocalDate startDate;
    private LocalDate endDate;
}
