package com.mycompany._thstudy.account.query.service;

import com.mycompany._thstudy.account.query.dto.response.AccountResponse;
import com.mycompany._thstudy.account.query.dto.response.AccountSummaryResponse;
import com.mycompany._thstudy.account.query.mapper.AccountMapper;
import com.mycompany._thstudy.exception.BusinessException;
import com.mycompany._thstudy.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountQueryService {

    private final AccountMapper accountMapper;

    public List<AccountResponse> getAccounts(String userEmail) {
        return accountMapper.findAllByUserEmail(userEmail);
    }

    public AccountResponse getAccount(String userEmail, Long accountId) {
        AccountResponse account = accountMapper.findByIdAndUserEmail(accountId, userEmail);
        if (account == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        return account;
    }

    public AccountSummaryResponse getAccountSummary(String userEmail) {
        List<AccountResponse> accounts = accountMapper.findAllByUserEmail(userEmail);
        long totalBalance = accounts.stream()
                .mapToLong(AccountResponse::getBalance)
                .sum();
        return new AccountSummaryResponse(totalBalance, accounts.size(), accounts);
    }
}
