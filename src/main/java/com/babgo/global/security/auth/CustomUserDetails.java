package com.babgo.global.security.auth;

import com.babgo.domain.user.User;
import com.babgo.domain.user.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security의 UserDetails 구현체
 *
 * User 엔티티를 Spring Security가 사용할 수 있는 형태로 변환합니다.
 * 인증/인가 과정에서 사용자 정보를 제공합니다.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;  // 실제 사용자 엔티티

    /**
     * TODO: 사용자의 권한 목록을 반환하는 메소드를 작성해야 합니다
     * - user.getRole().getKey()로 권한 키 추출 (예: "ROLE_CUSTOMER")
     * - SimpleGrantedAuthority 객체로 변환
     * - Collections.singletonList()로 단일 권한 리스트 반환
     * - Spring Security가 권한 체크시 사용합니다
     *
     * @return 권한 컬렉션
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 구현 필요
        return null;
    }

    /**
     * TODO: 사용자의 비밀번호를 반환하는 메소드를 작성해야 합니다
     * - user.getPassword() 반환
     * - Spring Security가 비밀번호 검증시 사용합니다
     *
     * @return 암호화된 비밀번호
     */
    @Override
    public String getPassword() {
        // 구현 필요
        return null;
    }

    /**
     * TODO: 사용자의 username을 반환하는 메소드를 작성해야 합니다
     * - user.getUserId() 반환 (이메일이 아닌 userId 사용)
     * - Spring Security의 Principal 이름으로 사용됩니다
     *
     * @return 사용자 ID
     */
    @Override
    public String getUsername() {
        // 구현 필요
        return null;
    }

    /**
     * TODO: 계정 만료 여부를 반환하는 메소드를 작성해야 합니다
     * - true를 반환 (만료되지 않음)
     * - 필요시 user의 특정 필드로 만료 여부 체크 가능
     *
     * @return true (만료되지 않음)
     */
    @Override
    public boolean isAccountNonExpired() {
        // 구현 필요
        return true;
    }

    /**
     * TODO: 계정 잠김 여부를 반환하는 메소드를 작성해야 합니다
     * - true를 반환 (잠기지 않음)
     * - 필요시 로그인 실패 횟수 등으로 잠김 처리 가능
     *
     * @return true (잠기지 않음)
     */
    @Override
    public boolean isAccountNonLocked() {
        // 구현 필요
        return true;
    }

    /**
     * TODO: 자격증명(비밀번호) 만료 여부를 반환하는 메소드를 작성해야 합니다
     * - true를 반환 (만료되지 않음)
     * - 필요시 비밀번호 변경 주기 체크 가능
     *
     * @return true (만료되지 않음)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        // 구현 필요
        return true;
    }

    /**
     * TODO: 계정 활성화 여부를 반환하는 메소드를 작성해야 합니다
     * - user.getIsUserDeleted()가 false이면 활성화 (삭제되지 않음)
     * - !user.getIsUserDeleted() 반환
     *
     * @return true (활성화됨), false (비활성화됨)
     */
    @Override
    public boolean isEnabled() {
        // 구현 필요
        return true;
    }

    /**
     * TODO: 편의 메소드 - 사용자 ID를 반환합니다
     *
     * @return 사용자 ID
     */
    public String getUserId() {
        // 구현 필요
        return null;
    }

    /**
     * TODO: 편의 메소드 - 사용자 이메일을 반환합니다
     *
     * @return 이메일
     */
    public String getEmail() {
        // 구현 필요
        return null;
    }

    /**
     * TODO: 편의 메소드 - 사용자 권한을 반환합니다
     *
     * @return UserRole
     */
    public UserRole getRole() {
        // 구현 필요
        return null;
    }
}