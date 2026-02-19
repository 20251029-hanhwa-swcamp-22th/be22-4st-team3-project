package com.mycompany._thstudy.transaction.command.application.controller;

import com.mycompany._thstudy.common.dto.ApiResponse;
import com.mycompany._thstudy.transaction.command.application.dto.request.TransactionCreateRequest;
import com.mycompany._thstudy.transaction.command.application.dto.request.TransactionUpdateRequest;
import com.mycompany._thstudy.transaction.command.application.dto.response.TransactionCommandResponse;
import com.mycompany._thstudy.transaction.command.application.service.TransactionCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionCommandController {

  private final TransactionCommandService transactionCommandService;

  @PostMapping
  public ResponseEntity<ApiResponse<TransactionCommandResponse>> create(
          @AuthenticationPrincipal UserDetails userDetails,
          @Valid @RequestBody TransactionCreateRequest request) {
    // TODO: transactionCommandService.createTransaction(userDetails.getUsername(), request) → 201
    TransactionCommandResponse response = transactionCommandService.createTransaction(userDetails.getUsername(),request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<TransactionCommandResponse>> update(
          @AuthenticationPrincipal UserDetails userDetails,
          @PathVariable Long id,
          @Valid @RequestBody TransactionUpdateRequest request) {
    // TODO: transactionCommandService.updateTransaction(userDetails.getUsername(), id, request) → 200
    TransactionCommandResponse response = transactionCommandService.updateTransaction(userDetails.getUsername(),id,request);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(
          @AuthenticationPrincipal UserDetails userDetails,
          @PathVariable Long id) {
    // TODO: transactionCommandService.deleteTransaction(userDetails.getUsername(), id) → 204
    transactionCommandService.deleteTransaction(userDetails.getUsername(),id);
    return ResponseEntity.noContent().build();
  }
}
