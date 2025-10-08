package com.babgo.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 권한 Enum
 * - CUSTOMER: 고객
 * - OWNER: 사장
 * - MANAGER: 매니저
 * - ADMIN: 관리자
 * - MASTER: 마스터 관리자 (최고 권한)
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {
    CUSTOMER("ROLE_CUSTOMER", "고객", 1),
    OWNER("ROLE_OWNER", "사장", 2),
    MANAGER("ROLE_MANAGER", "매니저", 3),
    MASTER("ROLE_MASTER", "관리자", 4);

    private final String key;        // Spring Security에서 사용하는 권한 키 (ROLE_ 접두사 필수)
    private final String description; // 권한 설명
    private final int level;         // 권한 레벨 (높을수록 상위 권한) - MASTER(4) > MANAGER(3) > OWNER(2) > CUSTOMER(1)
}
