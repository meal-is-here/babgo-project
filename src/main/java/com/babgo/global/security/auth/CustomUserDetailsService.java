package com.babgo.global.security.auth;

import com.babgo.domain.user.User;
import com.babgo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security의 UserDetailsService 구현체
 *
 * 사용자 인증시 DB에서 사용자 정보를 조회합니다.
 * Spring Security가 자동으로 호출합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * TODO: username(userId)로 사용자를 조회하는 메소드를 작성해야 합니다
     * - userRepository.findById(username)로 사용자 조회
     * - 사용자가 없으면 UsernameNotFoundException 발생
     * - 사용자가 삭제된 경우(isUserDeleted == true) UsernameNotFoundException 발생
     * - 정상 사용자면 CustomUserDetails 객체로 감싸서 반환
     * - @Transactional(readOnly = true) 어노테이션 추가 권장
     *
     * @param username 사용자 ID (userId)
     * @return UserDetails (CustomUserDetails)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 구현 필요
        // 1. userRepository.findById(username)로 사용자 조회
        // 2. 없으면 UsernameNotFoundException 발생
        // 3. isUserDeleted == true이면 UsernameNotFoundException 발생
        // 4. new CustomUserDetails(user) 반환
        return null;
    }

    /**
     * TODO: email로 사용자를 조회하는 추가 메소드를 작성해야 합니다
     * - 로그인시 이메일로 조회가 필요할 수 있습니다
     * - userRepository에 findByEmail 메소드 추가 필요
     * - loadUserByUsername과 동일한 로직
     *
     * @param email 이메일
     * @return UserDetails (CustomUserDetails)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        // 구현 필요
        // 1. userRepository.findByEmail(email)로 사용자 조회 (메소드 추가 필요)
        // 2. 없으면 UsernameNotFoundException 발생
        // 3. isUserDeleted == true이면 UsernameNotFoundException 발생
        // 4. new CustomUserDetails(user) 반환
        return null;
    }
}