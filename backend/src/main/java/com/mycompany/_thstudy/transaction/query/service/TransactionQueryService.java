package com.mycompany._thstudy.transaction.query.service;

import com.mycompany._thstudy.transaction.query.dto.request.TransactionSearchRequest;
import com.mycompany._thstudy.exception.BusinessException;
import com.mycompany._thstudy.exception.ErrorCode;
import com.mycompany._thstudy.transaction.query.dto.response.CategoryRawSummary;
import com.mycompany._thstudy.transaction.query.dto.response.DailySummaryResponse;
import com.mycompany._thstudy.transaction.query.dto.response.MonthlySummaryResponse;
import com.mycompany._thstudy.transaction.query.dto.response.TransactionListResponse;
import com.mycompany._thstudy.transaction.query.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionQueryService {

  private final TransactionMapper transactionMapper;

  public List<TransactionListResponse> getTransactions(String userEmail, TransactionSearchRequest req) {
    if (req.getStartDate() == null) {
      req.setStartDate(LocalDate.now().withDayOfMonth(1));
    }
    if (req.getEndDate() == null) {
      req.setEndDate(LocalDate.now());
    }
    return transactionMapper.findByFilter(userEmail, req);
  }

  public MonthlySummaryResponse getMonthlySummary(String userEmail, int year, int month) {

    List<CategoryRawSummary> rawList =
        transactionMapper.findMonthlySummary(userEmail, year, month);

    // INCOME / EXPENSE 분리
    List<CategoryRawSummary> incomeRaw = rawList.stream()
        .filter(r -> "INCOME".equals(r.getType())).toList();
    List<CategoryRawSummary> expenseRaw = rawList.stream()
        .filter(r -> "EXPENSE".equals(r.getType())).toList();

    // 총합 계산
    long totalIncome  = incomeRaw.stream().mapToLong(CategoryRawSummary::getAmount).sum();
    long totalExpense = expenseRaw.stream().mapToLong(CategoryRawSummary::getAmount).sum();

    // CategorySummary 변환 (비율 포함)
    List<MonthlySummaryResponse.CategorySummary> incomeSummary = incomeRaw.stream()
        .map(r -> new MonthlySummaryResponse.CategorySummary(
            r.getCategoryName(), r.getAmount(),
            totalIncome == 0 ? 0 : (double) r.getAmount() / totalIncome * 100
        )).toList();

    List<MonthlySummaryResponse.CategorySummary> expenseSummary = expenseRaw.stream()
        .map(r -> new MonthlySummaryResponse.CategorySummary(
            r.getCategoryName(), r.getAmount(),
            totalExpense == 0 ? 0 : (double) r.getAmount() / totalExpense * 100
        )).toList();

    return new MonthlySummaryResponse(
        year, month,
        totalIncome, totalExpense,
        totalIncome - totalExpense,
        incomeSummary, expenseSummary
    );
  }

  public List<DailySummaryResponse> getDailySummary(String userEmail, int year, int month) {
    return transactionMapper.findDailySummary(userEmail, year, month);
  }

  public List<TransactionListResponse> getRecentTransactions(String userEmail) {
    return transactionMapper.findRecentByUserEmail(userEmail);
  }

  public byte[] exportCsv(String userEmail, LocalDate startDate, LocalDate endDate) {
    TransactionSearchRequest req = new TransactionSearchRequest();
    req.setStartDate(startDate);
    req.setEndDate(endDate);
    List<TransactionListResponse> list = getTransactions(userEmail, req);
    StringBuilder sb = new StringBuilder("\uFEFF"); // BOM (한글 깨짐 방지)
    sb.append("날짜,유형,카테고리,금액,메모\n");
	for (TransactionListResponse t : list) {
	  sb.append("\"").append(t.getTransactionDate()).append("\"").append(",")
		  .append("INCOME".equals(t.getType()) ? "수입" : "지출").append(",")
		  .append(t.getCategoryName()).append(",")
		  .append(t.getAmount()).append(",")
		  // 메모(Description) 부분을 큰따옴표로 감싸도록 수정
		  .append("\"").append(t.getDescription() != null ? t.getDescription() : "").append("\"")
		  .append("\n");
	}
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  public byte[] exportXlsx(String userEmail, LocalDate startDate, LocalDate endDate) {
    TransactionSearchRequest req = new TransactionSearchRequest();
    req.setStartDate(startDate);
    req.setEndDate(endDate);
    List<TransactionListResponse> list = getTransactions(userEmail, req);

    try (Workbook workbook = new XSSFWorkbook();
       ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      Sheet sheet = workbook.createSheet("거래내역");

      // 1. 스타일 정의
      CellStyle headerStyle = createHeaderStyle(workbook);

      // 2. 헤더 생성
      String[] columns = {"날짜", "유형", "카테고리", "금액", "메모"};
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < columns.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(columns[i]);
        cell.setCellStyle(headerStyle); // 스타일 적용
      }

      // 3. 데이터 본문 생성
      int rowIdx = 1;
      for (TransactionListResponse t : list) {
        Row row = sheet.createRow(rowIdx++);

        row.createCell(0).setCellValue(t.getTransactionDate().toString());
        row.createCell(1).setCellValue("INCOME".equals(t.getType()) ? "수입" : "지출");
        row.createCell(2).setCellValue(t.getCategoryName());

        // 금액: 숫자로 입력해야 엑셀에서 합계 계산이 가능
        row.createCell(3).setCellValue(t.getAmount().doubleValue());

        row.createCell(4).setCellValue(t.getDescription() != null ? t.getDescription() : "");
      }

      // 4. 열 너비 자동 조정
      for (int i = 0; i < columns.length; i++) {
        sheet.autoSizeColumn(i);
      }

      workbook.write(out);
      return out.toByteArray();

    } catch (IOException e) {
      throw new BusinessException(ErrorCode.EXPORT_FAILED);
    }
  }

  // 헤더를 예쁘게 꾸며주는 보조 메서드
	private CellStyle createHeaderStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		// 배경색 설정 (밝은 회색)
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		// 테두리 설정
		style.setBorderBottom(BorderStyle.THIN);
		// 글꼴 설정 (굵게)
		Font font = workbook.createFont();
		font.setBold(true);
		style.setFont(font);
		// 가운데 정렬
		style.setAlignment(HorizontalAlignment.CENTER);
		return style;
	}
}
