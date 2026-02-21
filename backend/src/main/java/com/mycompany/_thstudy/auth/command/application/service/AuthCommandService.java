package com.mycompany._thstudy.auth.command.application.service;

import com.mycompany._thstudy.auth.command.application.dto.request.LoginRequest;
import com.mycompany._thstudy.auth.command.application.dto.request.SignupRequest;
import com.mycompany._thstudy.auth.command.application.dto.response.TokenResponse;
import com.mycompany._thstudy.auth.command.application.dto.response.SignupResponse;
import com.mycompany._thstudy.auth.command.domain.aggregate.RefreshToken;
import com.mycompany._thstudy.auth.command.domain.repository.RefreshTokenRepository;
import com.mycompany._thstudy.exception.BusinessException;
import com.mycompany._thstudy.exception.ErrorCode;
import com.mycompany._thstudy.security.JwtTokenProvider;
import com.mycompany._thstudy.user.command.domain.aggregate.User;
import com.mycompany._thstudy.user.command.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import com.mycompany._thstudy.category.command.domain.aggregate.Category;
import com.mycompany._thstudy.category.command.domain.repository.CategoryRepository;
import com.mycompany._thstudy.category.command.domain.repository.DefaultCategoryRepository;
import com.mycompany._thstudy.category.command.domain.aggregate.DefaultCategory;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthCommandService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final CategoryRepository categoryRepository;
  private final DefaultCategoryRepository defaultCategoryRepository;

  @Transactional
  public SignupResponse signup(SignupRequest request) {
    // 1. 이메일 중복 체크 → DUPLICATE_EMAIL
    if(userRepository.existsByEmail(request.getEmail())){
      throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
    }

    // 2. 비밀번호 암호화 후 User 엔티티 생성
    //    hint: passwordEncoder.encode(request.getPassword())
    //    hint: User.builder().email().password(encoded).nickname().build()
    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname(request.getNickname())
        .build();

    // 3. save() 반환값을 받아야 @GeneratedValue로 채워진 id 사용 가능
    //    hint: User savedUser = userRepository.save(user)
    User savedUser = userRepository.save(user);

	  List<DefaultCategory> defaults = defaultCategoryRepository.findAll();
	  defaults.forEach(dc -> categoryRepository.save(
		  Category.builder()
			  .user(savedUser)
			  .name(dc.getName())
			  .type(dc.getType())
			  .build()
	  ));


      // 4. savedUser의 id, email, nickname으로 SignupResponse 반환
    //    hint: return new SignupResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getNickname())
    return new SignupResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getNickname());
  }

  @Transactional
  public TokenResponse login(LoginRequest request) {
      // 1. findByEmail → USER_NOT_FOUND
      User user = userRepository.findByEmail(request.getEmail())
              .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
      // 2. passwordEncoder.matches() → LOGIN_FAILED
      if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
          throw new BusinessException(ErrorCode.LOGIN_FAILED);
      }
      // 3. jwtTokenProvider.createToken(email, role)
      String accessToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
      String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getRole().name());

      // 4. RefreshToken 저장
      RefreshToken tokenEntity = RefreshToken.builder()
          .userEmail(user.getEmail())
          .token(refreshToken)
          .expiryDate(new Date(System.currentTimeMillis() + jwtTokenProvider.getRefreshExpiration()))
          .build();

      refreshTokenRepository.save(tokenEntity);

      return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  /* refresh token 검증 후 새 토큰 발급 서비스 */
  public TokenResponse refreshToken(String provideRefreshToken) {

    // refresh token 유효성 검사
    jwtTokenProvider.validateToken(provideRefreshToken);

    // 전달 받은 refresh token에서 사용자 이름(username) 얻어오기
    String userEmail = jwtTokenProvider.getUserEmailFromJWTToken(provideRefreshToken);

    // DB에서 userEmail이 일치하는 행의 refresh token 조회
    RefreshToken storedToken = refreshTokenRepository.findByUserEmail(userEmail).orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 요청 시 전달 받은 token과
    // DB에 저장 된 토큰이 일치하는지 확인
    if(!storedToken.getToken().equals(provideRefreshToken)){
      throw new BusinessException(ErrorCode.INVALID_TOKEN);
    }

    // 요청 시 전달 받은 token의 만료 기간이 현재 시간보다 과거인지 확인
    // (만료 기간이 지났는지 확인)
    if(storedToken.getExpiryDate().before(new Date())){
      throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
    }

    // userEmail이 일치하는 회원(User) 조회
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 새 토큰 발급
    String accessToken = jwtTokenProvider.createToken(user.getEmail(),user.getRole().name());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(),user.getRole().name());

    // RefreshToken 엔티티 생성(저장용)
    RefreshToken tokenEntity = RefreshToken.builder()
        .userEmail(userEmail)
        .token(refreshToken)
        .expiryDate(new Date(System.currentTimeMillis() + jwtTokenProvider.getRefreshExpiration()))
        .build();

    //DB 저장 (PK 행이 이미 존재 -> UPDATE)
    refreshTokenRepository.save(tokenEntity);

    return TokenResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  @Transactional
  public void logout(String refreshToken){
      jwtTokenProvider.validateToken(refreshToken);

      String userEmail = jwtTokenProvider.getUserEmailFromJWTToken(refreshToken);

      refreshTokenRepository.deleteByUserEmail(userEmail);
  }
}
