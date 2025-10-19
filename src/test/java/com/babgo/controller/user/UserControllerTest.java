package com.babgo.controller.user;

import com.babgo.domain.user.User;
import com.babgo.repository.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.babgo.BabgoApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")  // 테스트 프로파일 활성화
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    @SuppressWarnings("rawtypes")
    private ValueOperations valueOperations;

//    @MockitoBean
//    private com.babgo.repository.ai.config.GoogleAiController googleAiController;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Redis Mock 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
    }

    @Test
    @DisplayName("고객 회원가입 - 성공")
    void signUpCustomer_success() throws Exception {
        // given
        UserRequest.CustomerSignUpRequest request = new UserRequest.CustomerSignUpRequest(
                "customer@example.com",
                "password123",
                "홍길동",
                "길동이",
                "010-1234-5678"
        );

        // when & then
        mockMvc.perform(post("/v1/auth/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("customer@example.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.nickname").value("길동이"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.data.publicId").exists())
                .andExpect(header().exists("Authorization"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    @DisplayName("사장 회원가입 - 성공")
    void signUpOwner_success() throws Exception {
        // given
        UserRequest.OwnerSignUpRequest request = new UserRequest.OwnerSignUpRequest(
                "owner@example.com",
                "password123",
                "김사장",
                "사장님",
                "010-9876-5432",
                "123-45-67890",
                "맛있는식당"
        );

        // when & then
        mockMvc.perform(post("/v1/auth/register/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("owner@example.com"))
                .andExpect(jsonPath("$.data.name").value("김사장"))
                .andExpect(jsonPath("$.data.nickname").value("사장님"))
                .andExpect(jsonPath("$.data.role").value("OWNER"))
                .andExpect(jsonPath("$.data.publicId").exists())
                .andExpect(header().exists("Authorization"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    @DisplayName("고객 회원가입 - 이메일 중복 실패")
    void signUpCustomer_duplicateEmail() throws Exception {
        // given
        User existingUser = User.ofCustomer(
                "duplicate@example.com",
                passwordEncoder.encode("password123"),
                "기존유저",
                "기존",
                "010-1111-2222"
        );
        userRepository.save(existingUser);

        UserRequest.CustomerSignUpRequest request = new UserRequest.CustomerSignUpRequest(
                "duplicate@example.com",
                "password456",
                "새유저",
                "새로운",
                "010-3333-4444"
        );

        // when & then
        mockMvc.perform(post("/v1/auth/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."));
    }

    @Test
    @DisplayName("고객 회원가입 - Validation 실패 (이메일 형식)")
    void signUpCustomer_invalidEmail() throws Exception {
        // given
        UserRequest.CustomerSignUpRequest request = new UserRequest.CustomerSignUpRequest(
                "invalid-email",
                "password123",
                "홍길동",
                "길동이",
                "010-1234-5678"
        );

        // when & then
        mockMvc.perform(post("/v1/auth/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("고객 로그인 - 성공")
    void userLogin_success() throws Exception {
        // given
        User user = User.ofCustomer(
                "login@example.com",
                passwordEncoder.encode("password123"),
                "로그인유저",
                "로그인",
                "010-5555-6666"
        );
        userRepository.save(user);

        UserRequest.LoginRequest request = new UserRequest.LoginRequest(
                "login@example.com",
                "password123"
        );

        // when & then
        mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(header().exists("Authorization"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    @DisplayName("사장 로그인 - 성공")
    void ownerLogin_success() throws Exception {
        // given
        User owner = User.ofOwner(
                "owner@example.com",
                passwordEncoder.encode("password123"),
                "사장님",
                "오너",
                "010-7777-8888"
        );
        userRepository.save(owner);

        UserRequest.LoginRequest request = new UserRequest.LoginRequest(
                "owner@example.com",
                "password123"
        );

        // when & then
        mockMvc.perform(post("/v1/auth/owner/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(header().exists("Authorization"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    @DisplayName("로그인 - 잘못된 비밀번호")
    void login_wrongPassword() throws Exception {
        // given
        User user = User.ofCustomer(
                "user@example.com",
                passwordEncoder.encode("correctPassword"),
                "유저",
                "닉네임",
                "010-1111-2222"
        );
        userRepository.save(user);

        UserRequest.LoginRequest request = new UserRequest.LoginRequest(
                "user@example.com",
                "wrongPassword"
        );

        // when & then
        mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 잘못되었습니다."));
    }

    @Test
    @DisplayName("로그인 - 존재하지 않는 이메일")
    void login_userNotFound() throws Exception {
        // given
        UserRequest.LoginRequest request = new UserRequest.LoginRequest(
                "notexist@example.com",
                "password123"
        );

        // when & then
        mockMvc.perform(post("/v1/auth/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 잘못되었습니다."));
    }

    @Test
    @DisplayName("고객 로그아웃 - 성공")
    void userLogout_success() throws Exception {
        mockMvc.perform(post("/v1/auth/user/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃 성공"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

    @Test
    @DisplayName("사장 로그아웃 - 성공")
    void ownerLogout_success() throws Exception {
        mockMvc.perform(post("/v1/auth/owner/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃 성공"))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }
}