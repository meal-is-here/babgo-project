package com.babgo.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 커스텀 권한 체크 어노테이션
 *
 * 사용 예시:
 * @RequireRole(UserRole.MASTER)
 * public void adminOnlyMethod() { ... }
 *
 * @RequireRole({UserRole.MASTER, UserRole.MANAGER})
 * public void managerOrAdminMethod() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /**
     * 허용할 권한 목록
     * 배열로 여러 권한 지정 가능
     */
    com.babgo.domain.user.UserRole[] value();
}