package com.mycompany._thstudy.account.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String name;
    private Long balance;
}
