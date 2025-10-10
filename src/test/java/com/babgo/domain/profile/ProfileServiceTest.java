package com.babgo.domain.profile;

import com.babgo.application.profile.ProfileInfo;
import com.babgo.controller.profile.dto.ProfileResponse;
import com.babgo.controller.profile.dto.ProfileUpdateRequest;
import com.babgo.domain.user.User;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileService profileService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.ofCustomer(
                "test@example.com",
                "password123!",
                "이다인",
                "다인",
                "010-1234-5678"
        );
    }

    @Test
    @DisplayName("프로필 조회 - 성공")
    void getMyProfile_success() {
        // given
        given(userRepository.findByUserIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(user));

        // when
        ProfileResponse response = profileService.getMyProfile(1L);

        // then
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("다인");
    }

    @Test
    @DisplayName("프로필 조회 - 실패 (존재하지 않는 유저)")
    void getMyProfile_fail_userNotFound() {
        // given
        given(userRepository.findByUserIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> profileService.getMyProfile(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("프로필 수정 - 성공 (비밀번호 포함)")
    void updateProfile_success() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "update@example.com",
                "newPassword123!",
                "이다인",
                "수정된닉네임",
                "010-1234-5678",
                true
        );
        ProfileInfo info = ProfileInfo.from(request);

        given(userRepository.findByUserIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(user));

        // when
        ProfileResponse response = profileService.updateProfile(1L, info);

        // then
        verify(userRepository, times(1)).findByUserIdAndDeletedAtIsNull(1L);
        assertThat(response.getEmail()).isEqualTo("update@example.com");
        assertThat(response.getNickname()).isEqualTo("수정된닉네임");
        assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("프로필 수정 - 실패 (유저 없음)")
    void updateProfile_fail_userNotFound() {
        // given
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "update@example.com",
                "newPassword123!",
                "이다인",
                "수정된닉네임",
                "010-1234-5678",
                true
        );
        ProfileInfo info = ProfileInfo.from(request);
        given(userRepository.findByUserIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> profileService.updateProfile(999L, info))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }
}