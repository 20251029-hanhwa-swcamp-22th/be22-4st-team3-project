package com.mycompany._thstudy.account.command.application.dto.response;

import com.mycompany._thstudy.account.command.domain.aggregate.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountCommandResponse {

    private Long id;
    private String name;
    private Long balance;

    public static AccountCommandResponse from(Account account) {
        return new AccountCommandResponse(
                account.getId(),
                account.getName(),
                account.getBalance()
        );
    }
}
