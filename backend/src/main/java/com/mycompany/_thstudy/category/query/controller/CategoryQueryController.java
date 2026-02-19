package com.mycompany._thstudy.category.query.controller;

import com.mycompany._thstudy.category.query.dto.response.CategoryResponse;
import com.mycompany._thstudy.category.query.service.CategoryQueryService;
import com.mycompany._thstudy.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryQueryController {

  private final CategoryQueryService categoryQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
          @AuthenticationPrincipal UserDetails userDetails) {
    // TODO: categoryQueryService.getCategories(userDetails.getUsername()) → ApiResponse.ok() → 200
    List<CategoryResponse> response = categoryQueryService.getCategories(userDetails.getUsername());
    return ResponseEntity.ok().body(ApiResponse.success(response));
  }
}
