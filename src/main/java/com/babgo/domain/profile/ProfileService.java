package com.babgo.domain.profile;

import com.babgo.controller.profile.dto.ProfileResponse;
import com.babgo.domain.user.User;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;

    // read profile
    public ProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                                  .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return ProfileResponse.from(user);
    }

    // update profile

}