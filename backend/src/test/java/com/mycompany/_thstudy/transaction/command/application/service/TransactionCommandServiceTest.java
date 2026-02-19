package com.mycompany._thstudy.transaction.command.application.service;

import com.mycompany._thstudy.category.command.domain.aggregate.Category;
import com.mycompany._thstudy.category.command.domain.aggregate.CategoryType;
import com.mycompany._thstudy.category.command.domain.repository.CategoryRepository;
import com.mycompany._thstudy.exception.BusinessException;
import com.mycompany._thstudy.exception.ErrorCode;
import com.mycompany._thstudy.transaction.command.application.dto.request.TransactionCreateRequest;
import com.mycompany._thstudy.transaction.command.domain.repository.TransactionRepository;
import com.mycompany._thstudy.user.command.domain.aggregate.User;
import com.mycompany._thstudy.user.command.domain.aggregate.UserRole;
import com.mycompany._thstudy.user.command.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionCommandServiceTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private TransactionCommandService transactionCommandService;

  @Test
  void createTransaction_negativeAmount_throwsBadRequest() {
    User user = User.builder()
        .id(1L)
        .email("user@test.com")
        .password("encoded")
        .nickname("user")
        .role(UserRole.USER)
        .build();

    Category category = Category.builder()
        .id(10L)
        .user(user)
        .name("식비")
        .type(CategoryType.EXPENSE)
        .build();

    TransactionCreateRequest request = new TransactionCreateRequest();
    ReflectionTestUtils.setField(request, "categoryId", 10L);
    ReflectionTestUtils.setField(request, "type", CategoryType.EXPENSE);
    ReflectionTestUtils.setField(request, "amount", 0L);
    ReflectionTestUtils.setField(request, "description", "점심");
    ReflectionTestUtils.setField(request, "transactionDate", LocalDate.now());

    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

    BusinessException ex = assertThrows(BusinessException.class,
        () -> transactionCommandService.createTransaction("user@test.com", request));

    assertEquals(ErrorCode.NEGATIVE_AMOUNT, ex.getErrorCode());
  }

  @Test
  void createTransaction_categoryTypeMismatch_throwsBadRequest() {
    User user = User.builder()
        .id(1L)
        .email("user@test.com")
        .password("encoded")
        .nickname("user")
        .role(UserRole.USER)
        .build();

    Category category = Category.builder()
        .id(10L)
        .user(user)
        .name("식비")
        .type(CategoryType.EXPENSE)
        .build();

    TransactionCreateRequest request = new TransactionCreateRequest();
    ReflectionTestUtils.setField(request, "categoryId", 10L);
    ReflectionTestUtils.setField(request, "type", CategoryType.INCOME);
    ReflectionTestUtils.setField(request, "amount", 1000L);
    ReflectionTestUtils.setField(request, "description", "테스트");
    ReflectionTestUtils.setField(request, "transactionDate", LocalDate.now());

    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
    when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

    BusinessException ex = assertThrows(BusinessException.class,
        () -> transactionCommandService.createTransaction("user@test.com", request));

    assertEquals(ErrorCode.CATEGORY_TYPE_MISMATCH, ex.getErrorCode());
  }
}

