package com.mycompany._thstudy.exception;

import com.mycompany._thstudy.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  // TODO: 구현 필요 핸들러 목록

  // 1. @ExceptionHandler(BusinessException.class)
  //    → errorCode.getStatus(), errorCode.getMessage()로 ApiResponse 반환
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleException(BusinessException e){
    logger.error("Business Exception : ", e);

    ErrorCode errorCode = e.getErrorCode();
    ApiResponse<Void> response = ApiResponse.failure(errorCode.getMessage(), errorCode.getCode());

    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }

  // 2. @ExceptionHandler(MethodArgumentNotValidException.class)
  //    → 400 + 유효성 검증 에러 메시지
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String,String>>> handleException(MethodArgumentNotValidException e){
    logger.error("MethodArgumentNotValidException : ", e);

    ErrorCode errorCode = ErrorCode.INVALID_INPUT;
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getFieldErrors().forEach(error ->{
      errors.put(error.getField(), error.getDefaultMessage());
    });

    ApiResponse<Map<String, String>> response = ApiResponse.failure("입력값 검증에 실패하였습니다.", errorCode.getCode(), errors);

    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }

  // 3. @ExceptionHandler(Exception.class)
  //    → 500 Internal Server Error (예상치 못한 에러)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<String>> handleException(Exception e){
    logger.error("Unhandled Exception : ", e);

    ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
    // 개발 환경에서 실제 에러 메시지 노출 (운영 배포 전 제거 필요)
    ApiResponse<String> response = ApiResponse.failure(errorCode.getMessage(), errorCode.getCode(), e.getMessage() + " / caused by: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));

    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }
}
