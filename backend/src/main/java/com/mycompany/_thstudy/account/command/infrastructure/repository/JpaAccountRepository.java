package com.mycompany._thstudy.account.command.infrastructure.repository;

import com.mycompany._thstudy.account.command.domain.aggregate.Account;
import com.mycompany._thstudy.account.command.domain.repository.AccountRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaAccountRepository extends JpaRepository<Account, Long>, AccountRepository {

    @Query("SELECT a FROM Account a WHERE a.user.email = :userEmail ORDER BY a.createdAt ASC")
    List<Account> findAllByUserEmail(@Param("userEmail") String userEmail);
}
