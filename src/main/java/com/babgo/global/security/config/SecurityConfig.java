package com.babgo.global.security.config;

import com.babgo.global.security.auth.CustomUserDetailsService;
import com.babgo.global.security.jwt.JwtAccessDeniedHandler;
import com.babgo.global.security.jwt.JwtAuthenticationEntryPoint;
import com.babgo.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스
 *
 * JWT 기반 인증/인가를 위한 Spring Security 설정입니다.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // @PreAuthorize, @PostAuthorize 등 메소드 레벨 시큐리티 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * TODO: 비밀번호 암호화를 위한 PasswordEncoder Bean을 생성해야 합니다
     * - BCryptPasswordEncoder 사용
     * - 회원가입시 비밀번호 암호화에 사용됩니다
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 구현 필요
        return null;
    }

    /**
     * TODO: AuthenticationManager Bean을 생성해야 합니다
     * - 로그인시 인증 처리를 담당합니다
     * - AuthenticationConfiguration에서 가져옵니다
     *
     * @param authenticationConfiguration AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // 구현 필요
        return null;
    }

    /**
     * TODO: SecurityFilterChain Bean을 생성해야 합니다
     * - Spring Security의 핵심 설정입니다
     *
     * 설정 내용:
     * 1. CSRF 비활성화 (JWT 사용으로 불필요)
     * 2. 세션 사용 안함 (STATELESS)
     * 3. HTTP 요청 인가 규칙 설정
     *    - /api/auth/** : 인증 없이 접근 가능 (회원가입, 로그인 등)
     *    - /h2-console/** : 개발용 H2 콘솔 접근 허용 (필요시)
     *    - 나머지 요청 : 인증 필요
     * 4. 예외 처리 설정
     *    - authenticationEntryPoint: 인증 실패시
     *    - accessDeniedHandler: 권한 부족시
     * 5. JWT 필터 추가
     *    - UsernamePasswordAuthenticationFilter 앞에 추가
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 구현 필요
        // http
        //     .csrf(AbstractHttpConfigurer::disable)  // CSRF 비활성화
        //     .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // 세션 사용 안함
        //     .authorizeHttpRequests(authorize -> authorize
        //         .requestMatchers("/api/auth/**").permitAll()  // 인증 없이 접근 가능
        //         .anyRequest().authenticated()  // 나머지는 인증 필요
        //     )
        //     .exceptionHandling(exception -> exception
        //         .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 인증 실패 처리
        //         .accessDeniedHandler(jwtAccessDeniedHandler)  // 권한 부족 처리
        //     )
        //     .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  // JWT 필터 추가
        //
        // return http.build();
        return null;
    }
}