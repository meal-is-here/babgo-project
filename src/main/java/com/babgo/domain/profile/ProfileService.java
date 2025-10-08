package com.babgo.domain.profile;

import com.babgo.domain.user.User;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;

    /**
     * 프로필 정보 업데이트
     */
    @Transactional
    public void updateProfile(User user, String nickname, String phoneNumber, Boolean isProfilePublic) {
        // 닉네임 변경 시 중복 체크
        if (!user.getNickname().equals(nickname)) {
            validateNicknameDuplication(nickname);
        }

        // 프로필 업데이트
        user.updateUserInfo(nickname, phoneNumber);
        user.updateProfilePublic(isProfilePublic);

        log.info("프로필 업데이트 완료: userId={}, nickname={}", user.getUserId(), nickname);
    }

    /**
     * 닉네임 중복 검증
     */
    private void validateNicknameDuplication(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    // TODO: 추가 프로필 관련 비즈니스 로직
    // - 프로필 이미지 업데이트
    // - 프로필 공개/비공개 설정
}