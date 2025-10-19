package com.babgo.domain.user;

import com.babgo.controller.user.UserRequest;
import com.babgo.controller.user.UserResponse;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 등록 서비스
 * - 회원가입 처리 (고객/사장)
 * - 이메일 중복 검증
 * - 비밀번호 암호화
 * - 인증/인가 로직은 AuthenticationService로 분리됨
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 고객 회원가입
     * @param request 회원가입 요청
     * @return 회원가입 응답
     */
    @Transactional
    public UserResponse.SignUpResponse signUpCustomer(UserRequest.CustomerSignUpRequest request) {
        // 이메일 중복 검증
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // CUSTOMER 권한으로 사용자 생성
        User user = User.ofCustomer(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getNickname(),
                request.getPhoneNumber()
        );

        User savedUser = userRepository.save(user);
        log.info("고객 회원가입 완료 - userId: {}, email: {}", savedUser.getUserId(), savedUser.getEmail());

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

    /**
     * 사장 회원가입
     * @param request 회원가입 요청
     * @return 회원가입 응답
     */
    @Transactional
    public UserResponse.SignUpResponse signUpOwner(UserRequest.OwnerSignUpRequest request) {
        // 이메일 중복 검증
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        // OWNER 권한으로 사용자 생성
        User user = User.ofOwner(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName(),
                request.getNickname(),
                request.getPhoneNumber()
        );

        User savedUser = userRepository.save(user);
        log.info("사장 회원가입 완료 - userId: {}, email: {}", savedUser.getUserId(), savedUser.getEmail());

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
}