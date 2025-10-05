package com.babgo.domain.user;

import com.babgo.application.user.UserDetailInfo;
import com.babgo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 인증시 DB에서 사용자 정보를 조회합니다.
 * Spring Security가 자동으로 호출합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    //userId로 사용자를 조회하는 메소드
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        if (user.getIsUserDeleted()) {
            throw new UsernameNotFoundException("삭제된 사용자입니다: " + username);
        }

        return new UserDetailInfo(user);
    }
}