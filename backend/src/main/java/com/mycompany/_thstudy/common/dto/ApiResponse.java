package com.mycompany._thstudy.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiResponse<T> {

  private boolean success;
  private T data;
  private String message;
  private String errorCode;
  private LocalDateTime timestamp;


  public static <T> ApiResponse<T> success(T data) {
      // TODO: 성공 응답 메서드
      return ApiResponse.<T>builder()
          .success(true)
          .data(data)
          .timestamp(LocalDateTime.now())
          .build();
  }

  public static <T> ApiResponse<T> failure(String message, String errorCode) {
      // TODO: 실패 응답 메서드
      return ApiResponse.<T>builder()
          .success(false)
          .errorCode(errorCode)
          .message(message)
          .timestamp(LocalDateTime.now())
          .build();
  }

  // MethodArgumentNotValidException 처리를 위한 failure 메서드 오버로딩
  public static <T> ApiResponse<T> failure(String message, String errorCode, T data) {
    // TODO: 실패 응답 메서드
    return ApiResponse.<T>builder()
        .success(false)
        .errorCode(errorCode)
        .message(message)
        .data(data)
        .timestamp(LocalDateTime.now())
        .build();
  }

}
