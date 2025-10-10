package com.babgo.domain.profile;

import com.babgo.application.profile.ProfileInfo;
import com.babgo.controller.profile.dto.ProfileResponse;
import com.babgo.domain.user.User;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // read profile
    public ProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                                  .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return ProfileResponse.from(user);
    }

    // update profile
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileInfo info) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                                  .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String encodedPassword = null;
        if (info.getPassword() != null && !info.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(info.getPassword());
        }

        user.updateProfile(
                info.getEmail(),
                encodedPassword,
                info.getName(),
                info.getNickname(),
                info.getPhoneNumber(),
                info.getIsProfilePublic()
        );

        return ProfileResponse.from(user);
    }
}