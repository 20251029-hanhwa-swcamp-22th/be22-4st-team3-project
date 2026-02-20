package com.mycompany._thstudy.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
      // 1. Authorization 헤더에서 Bearer 토큰 추출
      String token = getJwtFromRequest(request);
      // 2. validateToken → getUserIdFromToken
      try {
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
          // 3. 토큰에서 userEmail 추출
          String userEmail = jwtTokenProvider.getUserEmailFromJWTToken(token);
          // 4. userEmail 사용자 정보(UserDetails) 로드 (DB 등에서 조회)
          UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

          // 5. Authentication 객체 생성 (권한 정보 포함)
          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities()
              );

          // 6. SecurityContextHolder에 Authentication 객체 저장
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (BadCredentialsException e) {
        // 토큰이 만료되었거나 유효하지 않은 경우 인증을 설정하지 않고 통과시킨다.
        // permitAll() 엔드포인트(예: /api/auth/refresh)는 정상 접근되고,
        // authenticated() 엔드포인트는 이후 Spring Security가 401을 반환한다.
      }
      filterChain.doFilter(request, response);
    }

  private String getJwtFromRequest(HttpServletRequest request) {
      String bearerToken = request.getHeader("Authorization");

      if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
        return bearerToken.substring(7);
      }
      return null;
  }

}
