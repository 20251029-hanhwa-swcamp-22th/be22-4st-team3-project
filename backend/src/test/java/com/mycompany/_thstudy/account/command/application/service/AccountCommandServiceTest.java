package com.mycompany._thstudy.account.command.application.service;

import com.mycompany._thstudy.account.command.application.dto.request.AccountCreateRequest;
import com.mycompany._thstudy.account.command.application.dto.request.AccountUpdateRequest;
import com.mycompany._thstudy.account.command.application.dto.response.AccountCommandResponse;
import com.mycompany._thstudy.account.command.domain.aggregate.Account;
import com.mycompany._thstudy.account.command.domain.repository.AccountRepository;
import com.mycompany._thstudy.exception.BusinessException;
import com.mycompany._thstudy.exception.ErrorCode;
import com.mycompany._thstudy.user.command.domain.aggregate.User;
import com.mycompany._thstudy.user.command.domain.aggregate.UserRole;
import com.mycompany._thstudy.user.command.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountCommandServiceTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private AccountCommandService accountCommandService;

  @Test
  void createAccount_success() {
    User user = User.builder()
        .id(1L)
        .email("user@test.com")
        .password("encoded")
        .nickname("user")
        .role(UserRole.USER)
        .build();

    AccountCreateRequest request = new AccountCreateRequest();
    ReflectionTestUtils.setField(request, "name", "생활비 계좌");
    ReflectionTestUtils.setField(request, "balance", 10000L);

    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    when(accountRepository.save(any(Account.class))).thenReturn(
        Account.builder()
            .id(10L)
            .user(user)
            .name("생활비 계좌")
            .balance(10000L)
            .build()
    );

    AccountCommandResponse response = accountCommandService.createAccount("user@test.com", request);

    assertEquals(10L, response.getId());
    assertEquals("생활비 계좌", response.getName());
    assertEquals(10000L, response.getBalance());

    ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).save(captor.capture());
    assertEquals("생활비 계좌", captor.getValue().getName());
    assertEquals(10000L, captor.getValue().getBalance());
  }

  @Test
  void updateAccount_accessDenied() {
    User owner = User.builder()
        .id(1L)
        .email("owner@test.com")
        .password("encoded")
        .nickname("owner")
        .role(UserRole.USER)
        .build();

    Account account = Account.builder()
        .id(100L)
        .user(owner)
        .name("기존 계좌")
        .balance(1000L)
        .build();

    AccountUpdateRequest request = new AccountUpdateRequest();
    ReflectionTestUtils.setField(request, "name", "변경 계좌");
    ReflectionTestUtils.setField(request, "balance", 2000L);

    when(accountRepository.findById(100L)).thenReturn(Optional.of(account));

    BusinessException ex = assertThrows(BusinessException.class,
        () -> accountCommandService.updateAccount("other@test.com", 100L, request));

    assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
  }
}

