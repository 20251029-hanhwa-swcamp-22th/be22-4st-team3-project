package com.mycompany._thstudy.auth.command.application.controller;

import com.mycompany._thstudy.auth.command.application.dto.request.LoginRequest;
import com.mycompany._thstudy.auth.command.application.dto.request.SignupRequest;
import com.mycompany._thstudy.auth.command.application.dto.response.TokenResponse;
import com.mycompany._thstudy.auth.command.application.dto.response.SignupResponse;
import com.mycompany._thstudy.auth.command.application.service.AuthCommandService;
import com.mycompany._thstudy.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthCommandController {

  private final AuthCommandService authCommandService;

  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
    SignupResponse signupResponse = authCommandService.signup(request);
    return buildSignupResponse(signupResponse);
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
    TokenResponse tokenResponse = authCommandService.login(request);
    return buildTokenResponse(tokenResponse);
  }

  /* 로그 아웃 */
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @CookieValue(name = "refreshToken", required = false) String refreshToken){

    // refreshToken이 존재할 경우 == 로그인 상태
    if(refreshToken !=null){
      authCommandService.logout(refreshToken); // DB refreshToken 삭제
    }

    ResponseCookie deleteCookie = createDeleteRefreshTokenCookie();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
        .body(ApiResponse.success(null));
  }

  /* refresh Token을 요청 시 전달 받아
   * 인증된 토큰이면 새 access, refresh Token을 발급해서 반환
   * */
  @PostMapping("/refresh")
  private ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
      @CookieValue(name = "refreshToken", required = false) String refreshToken){

    // 쿠키에 refresh token이 없는 경우
    if(refreshToken==null){
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 반환
    }

    /* refresh token이 문제가 없다면
     * 새 access/refresh token 발급 */
    TokenResponse tokenResponse = authCommandService.refreshToken(refreshToken);

    return buildTokenResponse(tokenResponse);
  }

  /* 회원가입 성공 응답 (201 Created) 생성
   * hint: login의 buildTokenResponse() 패턴을 참고
   * hint: ResponseEntity.status(HttpStatus.CREATED)
   *         .body(ApiResponse.success(signupResponse))
   */
  private ResponseEntity<ApiResponse<SignupResponse>> buildSignupResponse(SignupResponse signupResponse) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(signupResponse));
  }

  /* accessToken 과 refreshToken을 body와 쿠키에 담아 반환 */
  private ResponseEntity<ApiResponse<TokenResponse>> buildTokenResponse(TokenResponse tokenResponse) {
    ResponseCookie cookie = createRefreshTokenCookie(tokenResponse.getRefreshToken());  // refreshToken 쿠키 생성
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(ApiResponse.success(tokenResponse));
  }

  /* refreshToken 쿠키 삭제용 설정 */
  private ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)                     // HttpOnly 속성 설정 (JavaScript 에서 접근 불가)
        // .secure(true)                    // HTTPS 환경일 때만 전송 (운영 환경에서 활성화 권장)
        .path("/")                          // 쿠키 범위 : 전체 경로
        .maxAge(Duration.ofDays(7))        // 쿠키 만료 기간 : 7일
        .sameSite("Strict")                 // CSRF 공격 방어를 위한 SameSite 설정
        .build();
  }

  /* */
  private ResponseCookie createDeleteRefreshTokenCookie() {
    return ResponseCookie.from("refreshToken", "")
        .httpOnly(true)                     // HttpOnly 속성 설정 (JavaScript 에서 접근 불가)
        // .secure(true)                    // HTTPS 환경일 때만 전송 (운영 환경에서 활성화 권장)
        .path("/")                          // 쿠키 범위 : 전체 경로
        .maxAge(0)         // 쿠키 만료 기간 : 0 ->
        .sameSite("Strict")                 // CSRF 공격 방어를 위한 SameSite 설정
        .build();
  }

}
