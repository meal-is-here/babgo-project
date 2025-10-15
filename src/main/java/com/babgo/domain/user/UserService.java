package com.babgo.domain.user;

import com.babgo.controller.user.dto.UserRequest;
import com.babgo.controller.user.dto.UserResponse;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User 도메인 서비스
 * - 사용자 CRUD
 * - 비즈니스 로직 처리
 * - 인증/인가 로직은 UserAuthService로 분리됨
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

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

    /**
     * 사용자 조회 (userId)
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     */
    @Transactional(readOnly = true)
    public User findByUserId(Long userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 사용자 조회 (email)
     * @param email 이메일
     * @return 사용자 엔티티
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 이메일 존재 여부 확인
     * @param email 이메일
     * @return 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 사용자 정보 업데이트
     * @param userId 사용자 ID
     * @param email 이메일 (선택)
     * @param password 비밀번호 (선택)
     * @param name 이름 (선택)
     * @param nickname 닉네임 (선택)
     * @param phoneNumber 전화번호 (선택)
     * @param isProfilePublic 프로필 공개 여부 (선택)
     * @return 업데이트된 사용자
     */
    @Transactional
    public User updateProfile(
            Long userId,
            String email,
            String password,
            String name,
            String nickname,
            String phoneNumber,
            Boolean isProfilePublic
    ) {
        User user = findByUserId(userId);

        // 이메일 변경 시 중복 확인
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
            }
        }

        // 비밀번호 암호화
        String encodedPassword = null;
        if (password != null && !password.isBlank()) {
            encodedPassword = passwordEncoder.encode(password);
        }

        // 프로필 업데이트
        user.updateProfile(email, encodedPassword, name, nickname, phoneNumber, isProfilePublic);

        log.info("프로필 업데이트 완료: userId={}", userId);
        return user;
    }

    /**
     * 사용자 소프트 삭제
     * @param userId 사용자 ID
     * @param deletedBy 삭제 요청자
     */
    @Transactional
    public void deleteUser(Long userId, String deletedBy) {
        User user = findByUserId(userId);

        if (Boolean.TRUE.equals(user.getIsUserDeleted())) {
            throw new CustomException(ErrorCode.USER_ALREADY_DELETED);
        }

        user.markAsDeleted();
        log.info("사용자 삭제 완료: userId={}, deletedBy={}", userId, deletedBy);
    }

    /**
     * 사용자 복구
     * @param userId 사용자 ID
     * @return 복구된 사용자
     */
    @Transactional
    public User restoreUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getIsUserDeleted())) {
            throw new CustomException(ErrorCode.USER_NOT_DELETED);
        }

        user.restore();
        log.info("사용자 복구 완료: userId={}", userId);
        return user;
    }
}