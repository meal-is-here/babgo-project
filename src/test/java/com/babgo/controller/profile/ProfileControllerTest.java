package com.babgo.controller.profile;

import com.babgo.domain.user.User;
import com.babgo.domain.user.UserRole;
import com.babgo.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.babgo.BabgoApplication.class)
@AutoConfigureMockMvc
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("프로필 조회 - 성공 (인증된 사용자)")
    @WithMockUser(username = "1", roles = {"CUSTOMER"})
    void getMyProfile_success() throws Exception {
        // given
        User user = User.builder()
                .userId("1")
                .email("test@example.com")
                .password("encodedPw")
                .name("이다인")
                .nickname("다인")
                .phoneNumber("010-1234-5678")
                .isProfilePublic(true)
                .role(UserRole.CUSTOMER)
                .build();
        userRepository.save(user);

        // when & then
        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("프로필 조회를 성공했습니다."))
                .andExpect(jsonPath("$.data.name").value("이다인"))
                .andExpect(jsonPath("$.data.nickname").value("다인"))
                .andExpect(jsonPath("$.data.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.data.isProfilePublic").value(true));
    }

    @Test
    @DisplayName("프로필 조회 - 비로그인 사용자 (401 Unauthorized)")
    void getMyProfile_unauthorized() throws Exception {
        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }
}