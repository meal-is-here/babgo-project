package com.babgo.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 커스텀 권한 체크 어노테이션
 *
 * 사용 예시:
 * @RequireRole(UserRole.ADMIN)
 * public void adminOnlyMethod() { ... }
 *
 * @RequireRole({UserRole.ADMIN, UserRole.MANAGER})
 * public void managerOrAdminMethod() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})  // 메소드와 클래스에 사용 가능
@Retention(RetentionPolicy.RUNTIME)              // 런타임에 어노테이션 정보 유지
public @interface UserRole {
    /**
     * 허용할 권한 목록
     * 배열로 여러 권한 지정 가능
     */
    com.babgo.domain.user.UserRole[] value();

    // TODO: 이 어노테이션을 실제로 동작시키려면 Aspect 클래스를 만들어야 함
    // - @Aspect 클래스 생성
    // - @Around("@annotation(requireRole)") 메소드 작성
    // - SecurityContextHolder에서 현재 사용자 권한 확인
    // - 권한이 없으면 AccessDeniedException 발생
}