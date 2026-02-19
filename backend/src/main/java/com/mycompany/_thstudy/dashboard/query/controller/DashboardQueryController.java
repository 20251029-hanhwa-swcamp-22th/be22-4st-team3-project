package com.mycompany._thstudy.dashboard.query.controller;

import com.mycompany._thstudy.common.dto.ApiResponse;
import com.mycompany._thstudy.dashboard.query.dto.response.DashboardResponse;
import com.mycompany._thstudy.dashboard.query.service.DashboardQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardQueryController {

  private final DashboardQueryService dashboardQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
      @AuthenticationPrincipal UserDetails userDetails) {
    DashboardResponse response = dashboardQueryService.getDashboard(userDetails.getUsername());
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}

