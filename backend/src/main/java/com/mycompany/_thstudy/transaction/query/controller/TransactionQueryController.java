package com.mycompany._thstudy.transaction.query.controller;

import com.mycompany._thstudy.common.dto.ApiResponse;
import com.mycompany._thstudy.transaction.query.dto.request.TransactionSearchRequest;
import com.mycompany._thstudy.transaction.query.dto.response.DailySummaryResponse;
import com.mycompany._thstudy.transaction.query.dto.response.MonthlySummaryResponse;
import com.mycompany._thstudy.transaction.query.dto.response.TransactionListResponse;
import com.mycompany._thstudy.transaction.query.service.TransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionQueryController {


  private final TransactionQueryService transactionQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<TransactionListResponse>>> getTransactions(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(required = false) Long accountId,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long minAmount,
      @RequestParam(required = false) Long maxAmount) {

    TransactionSearchRequest req = new TransactionSearchRequest();
    req.setStartDate(startDate);
    req.setEndDate(endDate);
    req.setAccountId(accountId);
    req.setType(type);
    req.setCategoryId(categoryId);
    req.setKeyword(keyword);
    req.setMinAmount(minAmount);
    req.setMaxAmount(maxAmount);

    List<TransactionListResponse> response = transactionQueryService.getTransactions(userDetails.getUsername(), req);
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

	@GetMapping("/export/csv")
	public ResponseEntity<byte[]> exportCsv(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		byte[] csv = transactionQueryService.exportCsv(userDetails.getUsername(), startDate, endDate);
		String filename = buildFilename(startDate, endDate, "csv");

		// 빌더를 사용하여 Content-Disposition 헤더 생성
		String contentDisposition = ContentDisposition.attachment()
			.filename(filename, StandardCharsets.UTF_8)
			.build()
			.toString();

		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
			.contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
			.body(csv);
	}

	@GetMapping("/export/xlsx")
	public ResponseEntity<byte[]> exportXlsx(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		byte[] xlsx = transactionQueryService.exportXlsx(userDetails.getUsername(), startDate, endDate);
		String filename = buildFilename(startDate, endDate, "xlsx");

		// 빌더를 사용하여 Content-Disposition 헤더 생성
		String contentDisposition = ContentDisposition.attachment()
			.filename(filename, StandardCharsets.UTF_8)
			.build()
			.toString();

		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
			.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
			.body(xlsx);
	}

  private String buildFilename(LocalDate startDate, LocalDate endDate, String ext) {
  	LocalDate now = LocalDate.now();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");
    String start = (startDate != null ? startDate : now.withDayOfMonth(1)).format(fmt);
    String end = (endDate != null ? endDate : now).format(fmt);
    return "transactions_" + start + "_" + end + "." + ext;
  }
}
