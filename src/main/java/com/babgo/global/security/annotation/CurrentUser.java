package com.babgo.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 현재 인증된 사용자를 주입받기 위한 어노테이션
 *
 * 사용 예시:
 * - User 객체 전체를 받기:
 *   public void someMethod(@CurrentUser User user) { ... }
 *
 * - userId만 받기:
 *   public void someMethod(@CurrentUser Long userId) { ... }
 *
 * - 이메일만 받기:
 *   public void someMethod(@CurrentUser String email) { ... }
 *
 * 동작 방식:
 * - Spring Security의 SecurityContext에서 인증된 사용자 정보를 추출
 * - UserDetailInfo에서 userId를 가져와 UserRepository로 User 엔티티 조회
 * - 파라미터 타입에 따라 자동 변환
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}