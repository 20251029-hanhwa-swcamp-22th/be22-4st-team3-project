package com.mycompany._thstudy.category.command.application.controller;

import com.mycompany._thstudy.category.command.application.dto.request.CategoryCreateRequest;
import com.mycompany._thstudy.category.command.application.dto.request.CategoryUpdateRequest;
import com.mycompany._thstudy.category.command.application.dto.response.CategoryCommandResponse;
import com.mycompany._thstudy.category.command.application.service.CategoryCommandService;
import com.mycompany._thstudy.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryCommandController {

  private final CategoryCommandService categoryCommandService;

  @PostMapping
  public ResponseEntity<ApiResponse<CategoryCommandResponse>> create(
          @AuthenticationPrincipal UserDetails userDetails,
          @Valid @RequestBody CategoryCreateRequest request) {
    // TODO: categoryCommandService.createCategory(userDetails.getUsername(), request) → 201
    CategoryCommandResponse response = categoryCommandService.createCategory(userDetails.getUsername(), request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<CategoryCommandResponse>> update(
          @AuthenticationPrincipal UserDetails userDetails,
          @PathVariable Long id,
          @Valid @RequestBody CategoryUpdateRequest request) {
    // TODO: categoryCommandService.updateCategory(userDetails.getUsername(), id, request) → 200
    CategoryCommandResponse response = categoryCommandService.updateCategory(userDetails.getUsername(), id, request);
    return ResponseEntity.ok().body(ApiResponse.success(response));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(
          @AuthenticationPrincipal UserDetails userDetails,
          @PathVariable Long id) {
    // TODO: categoryCommandService.deleteCategory(userDetails.getUsername(), id) → 204
    categoryCommandService.deleteCategory(userDetails.getUsername(), id);

    return ResponseEntity.noContent().build();
  }
}
