package com.babgo.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 고객(CUSTOMER) 권한이 필요한 메소드/클래스에 사용하는 어노테이션
 *
 * 사용 예시:
 * @RequireCustomer
 * public void orderFood() { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RequireRole(com.babgo.domain.user.UserRole.CUSTOMER)
public @interface RequireCustomer {
}
