package com.mycompany._thstudy.account.query.controller;

import com.mycompany._thstudy.account.query.dto.response.AccountResponse;
import com.mycompany._thstudy.account.query.dto.response.AccountSummaryResponse;
import com.mycompany._thstudy.account.query.service.AccountQueryService;
import com.mycompany._thstudy.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountQueryController {

    private final AccountQueryService accountQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AccountResponse> accounts = accountQueryService.getAccounts(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        AccountResponse account = accountQueryService.getAccount(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AccountSummaryResponse>> getAccountSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        AccountSummaryResponse summary = accountQueryService.getAccountSummary(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
