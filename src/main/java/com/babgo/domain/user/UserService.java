package com.babgo.domain.user;

import com.babgo.controller.user.dto.UserRequest;
import com.babgo.controller.user.dto.UserResponse;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.global.security.jwt.JwtTokenProvider;
import com.babgo.global.security.jwt.RedisService;
import com.babgo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final com.babgo.global.security.jwt.JwtProperties jwtProperties;

    // 고객 회원가입: 이메일 중복 검증 후 CUSTOMER 권한으로 사용자 생성 및 저장
    @Transactional
    public UserResponse.SignUpResponse signUpCustomer(UserRequest.CustomerSignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        User user = User.ofCustomer(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getNickname(),
                request.getPhoneNumber()
        );
        User savedUser = userRepository.save(user);
        log.info("고객 회원가입 완료: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());
        return UserResponse.SignUpResponse.of(
                savedUser.getPublicId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getNickname(),
                savedUser.getRole(),
                savedUser.getCreatedAt(),
                "고객 회원가입이 완료되었습니다"
        );
    }

    // 사장 회원가입: 이메일 중복 검증 후 OWNER 권한으로 사용자 생성 및 저장
    @Transactional
    public UserResponse.SignUpResponse signUpOwner(UserRequest.OwnerSignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        User user = User.ofOwner(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getNickname(),
                request.getPhoneNumber()
        );
        User savedUser = userRepository.save(user);
        log.info("사장 회원가입 완료: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());
        return UserResponse.SignUpResponse.of(
                savedUser.getPublicId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getNickname(),
                savedUser.getRole(),
                savedUser.getCreatedAt(),
                "사장 회원가입이 완료되었습니다"
        );
    }

    // 로그인: 이메일/비밀번호 검증 후 액세스 토큰(15분)과 리프레시 토큰(1일) 발급하고 Redis에 리프레시 토큰 저장
    @Transactional(readOnly = true)
    public UserResponse.LoginResponse login(UserRequest.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));
        if (Boolean.TRUE.equals(user.getIsUserDeleted())) {
            throw new CustomException(ErrorCode.USER_DELETED);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        redisService.saveRefreshToken(user.getUserId(), refreshToken, jwtProperties.getRefreshTokenExpiration());
        log.info("로그인 성공: userId={}, email={}, role={}", user.getUserId(), user.getEmail(), user.getRole());
        return UserResponse.LoginResponse.of(
                accessToken,
                refreshToken,
                user.getPublicId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }

    // 사용자 조회: userId로 삭제되지 않은 사용자 조회
    @Transactional(readOnly = true)
    public User findByUserId(Long userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 토큰 갱신: 리프레시 토큰의 블랙리스트 체크 후 유효성 검증하고 새로운 액세스 토큰만 재발급 (리프레시 토큰은 재사용)
    @Transactional(readOnly = true)
    public UserResponse.RefreshTokenResponse refreshToken(String refreshToken) {
        if (redisService.isBlacklisted(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        Long userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
        if (!redisService.validateRefreshToken(userId, refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        User user = findByUserId(userId);
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        );
        log.info("액세스 토큰 갱신 완료: userId={}", userId);
        return UserResponse.RefreshTokenResponse.of(newAccessToken, null);
    }

    // 로그아웃: 리프레시 토큰을 블랙리스트에 추가하여 무효화된 토큰의 추적 및 Redis에서 삭제
    public void logout(Long userId) {
        String refreshToken = redisService.getRefreshToken(userId);
        if (refreshToken != null) {
            redisService.addToBlacklist(refreshToken, jwtProperties.getRefreshTokenExpiration());
        }
        redisService.deleteRefreshToken(userId);
        log.info("로그아웃 완료: userId={}", userId);
    }
}
