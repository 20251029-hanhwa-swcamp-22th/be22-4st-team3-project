package com.mycompany._thstudy.account.command.domain.repository;

import com.mycompany._thstudy.account.command.domain.aggregate.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(Long id);

    void deleteById(Long id);

    List<Account> findAllByUserEmail(String userEmail);
}
