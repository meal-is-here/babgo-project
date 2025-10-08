package com.babgo.application.user;

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
 * User 엔티티를 Spring Security가 사용할 수 있는 형태로 변환합니다.
 * 인증/인가 과정에서 사용자 정보를 제공합니다.
 */
@Getter
@RequiredArgsConstructor
public class UserDetailInfo implements UserDetails {

    private final User user;  // 실제 사용자 엔티티

    // 사용자의 권한 목록을 반환하는 메소드
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority(user.getRole().getKey())
        );
    }

    // 사용자의 비밀번호를 반환하는 메소드
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    //사용자의 username을 반환하는 메소드
    @Override
    public String getUsername() {
        return String.valueOf(user.getUserId());
    }

    //계정 만료 여부를 반환하는 메소드 (새로운 아이디어?랄까나)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //계정 잠김 여부
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    //자격증명(비밀번호) 만료 여부를 반환하는 메소드
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //계정 활성화 여부를 반환하는 메소드
    @Override
    public boolean isEnabled() {
        return !user.getIsUserDeleted();
    }

    //사용자 ID를 반환메소드
    public String getUserId() {
        return String.valueOf(user.getUserId());
    }

    //편의 메소드 - 사용자 이메일을 반환
    public String getEmail() {
        return user.getEmail();
    }

    //편의 메소드 - 사용자 권한을 반환
    public UserRole getRole() {
        return user.getRole();
    }
}