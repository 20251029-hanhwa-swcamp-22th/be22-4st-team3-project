package com.mycompany._thstudy.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.util.Date;

@Component
public class JwtTokenProvider {

  // TODO: 구현 필요 항목
  // - @Value("${jwt.secret}") String secret
  @Value("${jwt.secret}")
  private String jwtSecret;
  // - @Value("${jwt.expiration}") long expiration
  @Value("${jwt.expiration}")
  private long jwtExpiration;

  @Value("${jwt.refresh-expiration}")
  private long jwtRefreshExpiration;

  // - SecretKey secretKey → @PostConstruct에서 초기화
  private SecretKey secretKey;

  @PostConstruct
  public void init(){
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
  }

  // access Token 생성
  public String createToken(String email, String role) {
    // TODO: Jwts.builder()로 JWT 생성
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpiration);

    return Jwts.builder()
      .subject(email)
      .claim("role",role)
      .issuedAt(now)
      .expiration(expiryDate)
      .signWith(secretKey)
      .compact();
  }

  // refresh Token 생성
  public String createRefreshToken(String email, String role) {
    // TODO: Jwts.builder()로 JWT 생성
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtRefreshExpiration);

    return Jwts.builder()
        .subject(email)
        .claim("role",role)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(secretKey)
        .compact();
  }

  public long getRefreshExpiration(){
    return jwtRefreshExpiration;
  }

  public String getUserEmailFromJWTToken(String token) {
    // TODO: 토큰 파싱 후 subject(userId) 추출
    Claims claims = Jwts.parser()
      .verifyWith(secretKey)
      .build()
      .parseSignedClaims(token)
      .getPayload();
    return claims.getSubject();
  }

  public boolean validateToken(String token) {
    // TODO: 토큰 유효성 검증
    try {
      Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
      return true;
    }
    catch (SecurityException | MalformedJwtException e) {
      throw new BadCredentialsException("Invalid JWT Token", e);
    }
    catch (ExpiredJwtException e) {
      throw new BadCredentialsException("Expired JWT Token", e);
    }
    catch (UnsupportedJwtException e) {
      throw new BadCredentialsException("Unsupported JWT Token", e);
    }
    catch (IllegalArgumentException e) {
      throw new BadCredentialsException("JWT Token claims empty", e);
    }
  }
}
