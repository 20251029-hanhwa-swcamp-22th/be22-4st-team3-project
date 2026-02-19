package com.mycompany._thstudy.account.command.application.service;

import com.mycompany._thstudy.account.command.application.dto.request.AccountCreateRequest;
import com.mycompany._thstudy.account.command.application.dto.request.AccountUpdateRequest;
import com.mycompany._thstudy.account.command.application.dto.response.AccountCommandResponse;
import com.mycompany._thstudy.account.command.domain.aggregate.Account;
import com.mycompany._thstudy.account.command.domain.repository.AccountRepository;
import com.mycompany._thstudy.exception.BusinessException;
import com.mycompany._thstudy.exception.ErrorCode;
import com.mycompany._thstudy.user.command.domain.aggregate.User;
import com.mycompany._thstudy.user.command.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountCommandService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountCommandResponse createAccount(String userEmail, AccountCreateRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Account account = Account.builder()
                .user(user)
                .name(request.getName())
                .balance(request.getBalance())
                .build();

        return AccountCommandResponse.from(accountRepository.save(account));
    }

    public AccountCommandResponse updateAccount(String userEmail, Long accountId, AccountUpdateRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        account.updateName(request.getName());
        account.updateBalance(request.getBalance());

        return AccountCommandResponse.from(accountRepository.save(account));
    }

    public void deleteAccount(String userEmail, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        accountRepository.deleteById(accountId);
    }
}
