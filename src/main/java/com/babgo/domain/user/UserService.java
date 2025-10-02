package com.babgo.domain.user;

import com.babgo.controller.user.UserRequest;
import com.babgo.controller.user.UserResponse;
import com.babgo.global.security.jwt.JwtProperties;
import com.babgo.global.security.jwt.JwtTokenProvider;
import com.babgo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
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
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;

    /**
     * 고객 회원가입
     */
    @Transactional
    public UserResponse.SignUpResponse signUpCustomer(UserRequest.CustomerSignUpRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }

        // 2. UserEntity 엔티티 생성
        UserEntity user = UserEntity.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.CUSTOMER)
                .build();

        // 3. 저장
        UserEntity savedUser = userRepository.save(user);
        log.info("고객 회원가입 완료: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());

        // 4. 응답 반환
        return UserResponse.SignUpResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .nickname(savedUser.getNickname())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .message("고객 회원가입이 완료되었습니다")
                .build();
    }

    /**
     * 가게 사장 회원가입
     */
    @Transactional
    public UserResponse.SignUpResponse signUpOwner(UserRequest.OwnerSignUpRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다");
        }

        // 2. UserEntity 엔티티 생성 (OWNER 권한)
        UserEntity user = UserEntity.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(request.getNickname())
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.OWNER)
                .build();

        // 3. 저장
        UserEntity savedUser = userRepository.save(user);
        log.info("사장 회원가입 완료: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());

        // 4. 응답 반환
        return UserResponse.SignUpResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .nickname(savedUser.getNickname())
                .role(savedUser.getRole())
                .createdAt(savedUser.getCreatedAt())
                .message("사장 회원가입이 완료되었습니다")
                .build();
    }

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public UserResponse.LoginResponse login(UserRequest.LoginRequest request) {
        // 1. 이메일로 사용자 조회
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다"));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다");
        }

        // 3. JWT Access Token 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        );

        log.info("로그인 성공: userId={}, email={}, role={}", user.getUserId(), user.getEmail(), user.getRole());

        return UserResponse.LoginResponse.builder()
                .accessToken(accessToken)
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    // TODO: 필요한 추가 메소드를 작성하세요
    // - 사용자 정보 조회
    // - 사용자 정보 수정
    // - 비밀번호 변경
    // - 토큰 갱신 (Refresh Token 구현시)
}
