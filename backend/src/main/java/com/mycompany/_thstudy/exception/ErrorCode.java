package com.mycompany._thstudy.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  // 400
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "BAD_REQUEST_001", "잘못된 입력 값입니다."),
  DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "BAD_REQUEST_002", "이미 사용 중인 이메일입니다."),
  LOGIN_FAILED(HttpStatus.BAD_REQUEST, "BAD_REQUEST_003","이메일 또는 비밀번호가 올바르지 않습니다."),
  NEGATIVE_AMOUNT(HttpStatus.BAD_REQUEST, "BAD_REQUEST_004","거래 금액은 0보다 커야합니다."),
  CATEGORY_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "BAD_REQUEST_005","카테고리 유형과 거래 유형이 일치하지 않습니다"),
  INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "BAD_REQUEST_006", "계좌 잔액이 부족합니다."),
  BALANCE_WOULD_BE_NEGATIVE(HttpStatus.BAD_REQUEST, "BAD_REQUEST_007", "해당 거래를 삭제하면 계좌 잔액이 0원 미만이 되어 불가합니다."),

  // 401
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_001", "유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_002", "만료된 토큰입니다."),

  // 403
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "FORBIDDEN_001", "접근 권한이 없습니다."),

  // 404
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_001",  "사용자를 찾을 수 없습니다."),
  CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_002", "카테고리를 찾을 수 없습니다."),
  TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_003", "거래 내역을 찾을 수 없습니다."),
  ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND_004", "계좌를 찾을 수 없습니다."),

  // 409
  CATEGORY_HAS_TRANSACTIONS(HttpStatus.CONFLICT, "CONFLICT_001", "해당 카테고리에 거래가 존재하여 삭제할 수 없습니다."),
  CATEGORY_DUPLICATE_NAME(HttpStatus.CONFLICT, "CONFLICT_002", "이미 존재하는 카테고리 입니다."),

  // 500
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR_001", "서버 내부 오류가 발생했습니다."),
  EXPORT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR_002", "파일 내보내기에 실패했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
