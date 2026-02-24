package com.mycompany._thstudy.category.command.application.service;

import com.mycompany._thstudy.category.command.application.dto.request.CategoryCreateRequest;
import com.mycompany._thstudy.category.command.application.dto.request.CategoryUpdateRequest;
import com.mycompany._thstudy.category.command.application.dto.response.CategoryCommandResponse;
import com.mycompany._thstudy.category.command.application.mapper.CategoryDuplicateMapper;
import com.mycompany._thstudy.category.command.domain.aggregate.Category;
import com.mycompany._thstudy.category.command.domain.aggregate.CategoryType;
import com.mycompany._thstudy.category.command.domain.aggregate.DefaultCategory;
import com.mycompany._thstudy.category.command.domain.repository.CategoryRepository;
import com.mycompany._thstudy.category.command.domain.repository.DefaultCategoryRepository;
import java.util.List;
import com.mycompany._thstudy.exception.BusinessException;
import com.mycompany._thstudy.exception.ErrorCode;
import com.mycompany._thstudy.transaction.command.domain.aggregate.Transaction;
import com.mycompany._thstudy.transaction.command.domain.repository.TransactionRepository;
import com.mycompany._thstudy.user.command.domain.aggregate.User;
import com.mycompany._thstudy.user.command.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCommandService {

  private final CategoryRepository categoryRepository;
  private final DefaultCategoryRepository defaultCategoryRepository;
  private final TransactionRepository transactionRepository;
  private final UserRepository userRepository;
  private final CategoryDuplicateMapper categoryDuplicateMapper;

  public CategoryCommandResponse createCategory(String userEmail, CategoryCreateRequest request) {

    // 0. 중복 체크
    boolean isDuplicate = categoryDuplicateMapper.existsByUserEmailAndNameAndType(
        userEmail,request.getName(),request.getType().name()
    );

    if(isDuplicate){
      throw new BusinessException(ErrorCode.CATEGORY_DUPLICATE_NAME);
    }

    // 1. userRepository.findByEmail(userEmail) → USER_NOT_FOUND
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 2. Category.builder().user(user).name(request.getName()).type(request.getType()).build()
    Category category = Category.builder()
        .user(user)
        .name(request.getName())
        .type(request.getType())
        .build();

    // 3. categoryRepository.save() → 반환값으로 id 획득
    Category savedCategory = categoryRepository.save(category);

    // 4. CategoryCommandResponse 반환
    return CategoryCommandResponse.builder()
        .id(savedCategory.getId())
        .name(savedCategory.getName())
        .type(savedCategory.getType())
        .build();
  }

  public CategoryCommandResponse updateCategory(String userEmail, Long categoryId, CategoryUpdateRequest request) {

    // 1. categoryRepository.findById(categoryId) → CATEGORY_NOT_FOUND
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

    // 2. category.getUser().getEmail()와 userEmail 비교 → ACCESS_DENIED
    if (!category.getUser().getEmail().equals(userEmail)) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    // 수정 전 카테고리 중복 체크
    boolean isDuplicate = categoryDuplicateMapper.existsByUserEmailAndNameAndTypeExcludeId(
        userEmail,
        request.getName(),
        category.getType().name(),
        categoryId
    );

    if (isDuplicate){
      throw new BusinessException(ErrorCode.CATEGORY_DUPLICATE_NAME);
    }

    // 3. category.updateName(request.getName())
    // TODO: Category.updateName() 구현 필요
    category.updateName(request.getName());

    // 4. CategoryCommandResponse 반환
    return CategoryCommandResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .type(category.getType())
        .build();
  }

  public void deleteCategory(String userEmail, Long categoryId) {

    // 1. findById(categoryId) → CATEGORY_NOT_FOUND
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

    // 2. category.getUser().getEmail()와 userEmail 비교 → ACCESS_DENIED
    if (!category.getUser().getEmail().equals(userEmail)) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    // 3. transactionRepository.existsByCategoryId(categoryId) → CATEGORY_HAS_TRANSACTIONS
    // TODO: 구현
    if(transactionRepository.existsByCategoryId(categoryId)){
      throw new BusinessException(ErrorCode.CATEGORY_HAS_TRANSACTIONS);
    }

    // 4. categoryRepository.delete(category)
    // TODO: 구현
    categoryRepository.delete(category);
  }

  public void createDefaultCategories(User user) {
    List<DefaultCategory> defaults = defaultCategoryRepository.findAll();
    defaults.forEach(dc -> categoryRepository.save(
        Category.builder()
            .user(user)
            .name(dc.getName())
            .type(dc.getType())
            .build()
    ));
  }

}
