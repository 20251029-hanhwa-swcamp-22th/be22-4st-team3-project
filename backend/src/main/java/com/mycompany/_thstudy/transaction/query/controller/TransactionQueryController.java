package com.mycompany._thstudy.transaction.query.controller;

import com.mycompany._thstudy.common.dto.ApiResponse;
import com.mycompany._thstudy.transaction.query.dto.response.DailySummaryResponse;
import com.mycompany._thstudy.transaction.query.dto.response.MonthlySummaryResponse;
import com.mycompany._thstudy.transaction.query.dto.response.TransactionListResponse;
import com.mycompany._thstudy.transaction.query.mapper.TransactionMapper;
import com.mycompany._thstudy.transaction.query.service.TransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionQueryController {


  private final TransactionQueryService transactionQueryService;
  private final TransactionMapper transactionMapper;

  @GetMapping
  public ResponseEntity<ApiResponse<List<TransactionListResponse>>> getTransactions(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    // TODO: transactionQueryService.getTransactions(userDetails.getUsername(), startDate, endDate) → 200
    List<TransactionListResponse> response = transactionQueryService.getTransactions(userDetails.getUsername(),startDate,endDate);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/summary/{year}/{month}")
  public ResponseEntity<ApiResponse<MonthlySummaryResponse>> getMonthlySummary(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable int year,
      @PathVariable int month){

    // TODO: transactionQueryService.getMonthlySummary(userDetails.getUsername(), year, month) → 200
    MonthlySummaryResponse response = transactionQueryService.getMonthlySummary(
        userDetails.getUsername(),year,month
    );

    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/daily/{year}/{month}")
  public ResponseEntity<ApiResponse<List<DailySummaryResponse>>> getDailySummary(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable int year,
      @PathVariable int month) {
    List<DailySummaryResponse> response = transactionQueryService.getDailySummary(
        userDetails.getUsername(), year, month
    );
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
