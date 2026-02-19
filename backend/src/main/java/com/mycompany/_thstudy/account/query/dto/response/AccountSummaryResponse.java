package com.mycompany._thstudy.account.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryResponse {
    private Long totalBalance;
    private int accountCount;
    private List<AccountResponse> accounts;
}
