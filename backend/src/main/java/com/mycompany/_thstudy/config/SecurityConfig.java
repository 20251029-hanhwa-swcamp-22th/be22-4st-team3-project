package com.mycompany._thstudy.config;

import com.mycompany._thstudy.security.JwtAuthenticationFilter;
import com.mycompany._thstudy.security.JwtTokenProvider;
import com.mycompany._thstudy.security.RestAccessDeniedHandler;
import com.mycompany._thstudy.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // TODO: 구현
    // 1. csrf 비활성화
    http.csrf(AbstractHttpConfigurer::disable)
    // 2. sessionManagement → STATELESS
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(exception ->
            exception
                .authenticationEntryPoint(restAuthenticationEntryPoint)
                .accessDeniedHandler(restAccessDeniedHandler))
    // 3. authorizeHttpRequests: /api/auth/** → permitAll, 나머지 → authenticated
        .authorizeHttpRequests(auth ->
            auth.requestMatchers(HttpMethod.POST,"/api/auth/**").permitAll()
                .anyRequest().authenticated()
        )
    // 4. addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    /* CORS 설정 */
    http.cors(cors -> cors
        .configurationSource(corsConfigurationSource()));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    // TODO: BCryptPasswordEncoder 반환
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsFilter corsFilter() {
    return new CorsFilter(corsConfigurationSource());
  }

  @Bean
  public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOrigin("http://localhost:5173"); // 허용할 도메인
    config.addAllowedHeader("*"); // 모든 헤더 허용
    config.addAllowedMethod("*"); // 모든 HTTP 메소드 허용

    // HTTPOnly Cookie 사용
    config.setAllowCredentials(true);// 자격 증명(쿠키 등) 허용

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);// 모든 경로에 대해 설정
    return source;
  }
}
