package com.mycompany._thstudy.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
      // TODO: 구현
      // 1. Authorization 헤더에서 Bearer 토큰 추출
      String token = getJwtFromRequest(request);
      // 2. validateToken → getUserIdFromToken
      if(StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
        // 3. 토큰에서 userEmail 추출
        String userEmail = jwtTokenProvider.getUserEmailFromJWTToken(token);
        // 4. userEmail 사용자 정보(UserDetails) 로드 (DB 등에서 조회)
        // CustomUserDetailsService 를 통해 사용자 정보를 로드한다.
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        // 5. Authentication 객체 생성 (권한 정보 포함)
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );

        // 6. SecurityContextHolder에 Authentication 객체 저장
        // 이를 통해 이후 필터나 컨트롤러에서 인증된 사용자 정보를 사용할 수 있음 (@AuthenticationPrincipal 등)
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
      /* if문을 통과했다면 SecurityContextHolder의 Authentication이 설정 된 상태이고,
       * 통과하지 못했다면 해당 값이 비어있는 상태이다.
       * 이어지는 필터에서 인증 성공/실패가 가려진다. */
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
