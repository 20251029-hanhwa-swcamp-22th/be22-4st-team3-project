package com.mycompany._thstudy.transaction.query.mapper;

import com.mycompany._thstudy.transaction.query.dto.request.TransactionSearchRequest;
import com.mycompany._thstudy.transaction.query.dto.response.CategoryRawSummary;
import com.mycompany._thstudy.transaction.query.dto.response.DailySummaryResponse;
import com.mycompany._thstudy.transaction.query.dto.response.MonthlySummaryResponse;
import com.mycompany._thstudy.transaction.query.dto.response.TransactionListResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionMapper {

  List<TransactionListResponse> findByFilter(
          @Param("userEmail") String userEmail,
          @Param("req") TransactionSearchRequest req
  );

  List<CategoryRawSummary> findMonthlySummary(
      @Param("userEmail") String userEmail,
      @Param("year") int year,
      @Param("month") int month
  );

  List<DailySummaryResponse> findDailySummary(
      @Param("userEmail") String userEmail,
      @Param("year") int year,
      @Param("month") int month
  );

  List<TransactionListResponse> findRecentByUserEmail(
      @Param("userEmail") String userEmail
  );

}
