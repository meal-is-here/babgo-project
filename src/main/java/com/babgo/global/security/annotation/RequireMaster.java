package com.babgo.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 마스터(MASTER) 권한이 필요한 메소드/클래스에 사용하는 어노테이션
 *
 * 사용 예시:
 * @RequireMaster
 * public void manageSystem() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RequireRole(com.babgo.domain.user.UserRole.MASTER)
public @interface RequireMaster {
}
