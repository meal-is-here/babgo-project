package com.babgo.domain.user;

import com.babgo.controller.user.dto.UserRequest;
import com.babgo.controller.user.dto.UserResponse;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.global.security.jwt.JwtTokenProvider;
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

    /**
     * 고객 회원가입
     */
    @Transactional
    public UserResponse.SignUpResponse signUpCustomer(UserRequest.CustomerSignUpRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. User 엔티티 생성
        User user = User.ofCustomer(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getNickname(),
                request.getPhoneNumber()
        );

        // 3. 저장
        User savedUser = userRepository.save(user);
        log.info("고객 회원가입 완료: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());

        // 4. 응답 반환
        return UserResponse.SignUpResponse.of(
                String.valueOf(savedUser.getUserId()),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getNickname(),
                savedUser.getRole(),
                savedUser.getCreatedAt(),
                "고객 회원가입이 완료되었습니다"
        );
    }

    /**
     * 가게 사장 회원가입
     */
    @Transactional
    public UserResponse.SignUpResponse signUpOwner(UserRequest.OwnerSignUpRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. User 엔티티 생성 (OWNER 권한)
        User user = User.ofOwner(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getNickname(),
                request.getPhoneNumber()
        );

        // 3. 저장
        User savedUser = userRepository.save(user);
        log.info("사장 회원가입 완료: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());

        // 4. 응답 반환
        return UserResponse.SignUpResponse.of(
                String.valueOf(savedUser.getUserId()),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getNickname(),
                savedUser.getRole(),
                savedUser.getCreatedAt(),
                "사장 회원가입이 완료되었습니다"
        );
    }

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public UserResponse.LoginResponse login(UserRequest.LoginRequest request) {
        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        // 2. 삭제된 사용자 체크
        if (Boolean.TRUE.equals(user.getIsUserDeleted())) {
            throw new CustomException(ErrorCode.USER_DELETED);
        }

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 4. JWT Access Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        );

        log.info("로그인 성공: userId={}, email={}, role={}", user.getUserId(), user.getEmail(), user.getRole());

        return UserResponse.LoginResponse.of(
                accessToken,
                String.valueOf(user.getUserId()),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }

    /**
     * userId로 사용자 조회
     */
    @Transactional(readOnly = true)
    public User findByUserId(Long userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // TODO: 필요한 추가 메소드를 작성하세요
    // - 토큰 갱신 (Refresh Token 구현시)
}
