package com.mycompany._thstudy.account.command.application.controller;

import com.mycompany._thstudy.account.command.application.dto.request.AccountCreateRequest;
import com.mycompany._thstudy.account.command.application.dto.request.AccountUpdateRequest;
import com.mycompany._thstudy.account.command.application.dto.response.AccountCommandResponse;
import com.mycompany._thstudy.account.command.application.service.AccountCommandService;
import com.mycompany._thstudy.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountCommandController {

    private final AccountCommandService accountCommandService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountCommandResponse>> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AccountCreateRequest request) {
        AccountCommandResponse response = accountCommandService.createAccount(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountCommandResponse>> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody AccountUpdateRequest request) {
        AccountCommandResponse response = accountCommandService.updateAccount(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        accountCommandService.deleteAccount(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
