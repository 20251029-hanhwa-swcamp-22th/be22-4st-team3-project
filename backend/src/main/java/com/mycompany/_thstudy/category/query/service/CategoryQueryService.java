package com.mycompany._thstudy.category.query.service;

import com.mycompany._thstudy.category.query.dto.response.CategoryResponse;
import com.mycompany._thstudy.category.query.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryService {

  private final CategoryMapper categoryMapper;

  public List<CategoryResponse> getCategories(String userEmail) {
    // TODO: 구현
    // categoryMapper.findByUserEmail(userEmail) 호출 후 결과 반환

    return categoryMapper.findByUserEmail(userEmail);
  }
}
