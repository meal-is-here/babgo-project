package com.babgo.global.security.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @RequireRole 어노테이션을 처리하는 AOP Aspect
 *
 * 메소드 실행 전에 사용자의 권한을 체크합니다.
 * 권한이 없으면 AccessDeniedException을 발생시킵니다.
 */
@Slf4j
@Aspect
@Component
public class RoleCheckAspect {

    /**
     * TODO: @RequireRole 어노테이션이 붙은 메소드의 권한을 체크하는 메소드를 작성해야 합니다
     * - @Around 어노테이션으로 메소드 실행 전후를 제어합니다
     *
     * 처리 순서:
     * 1. SecurityContextHolder에서 현재 인증 정보 가져오기
     * 2. 인증되지 않았으면 AccessDeniedException 발생
     * 3. 메소드의 @RequireRole 어노테이션 추출
     * 4. 어노테이션에 정의된 필요 권한(UserRole[]) 추출
     * 5. 현재 사용자의 권한 추출
     * 6. 필요 권한 목록에 현재 사용자 권한이 포함되는지 확인
     * 7. 권한이 있으면 joinPoint.proceed() 실행
     * 8. 권한이 없으면 AccessDeniedException 발생
     *
     * @param joinPoint AOP Join Point
     * @return 메소드 실행 결과
     * @throws Throwable
     */
    @Around("@annotation(com.babgo.global.security.annotation.UserRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        // 구현 필요
        // 1. Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 2. if (authentication == null || !authentication.isAuthenticated()) {
        //        throw new AccessDeniedException("인증이 필요합니다");
        //    }
        // 3. MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //    RequireRole requireRole = signature.getMethod().getAnnotation(RequireRole.class);
        // 4. UserRole[] requiredRoles = requireRole.value();
        // 5. CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        //    UserRole userRole = userDetails.getRole();
        // 6. boolean hasRole = Arrays.asList(requiredRoles).contains(userRole);
        // 7. if (hasRole) { return joinPoint.proceed(); }
        // 8. else { throw new AccessDeniedException("접근 권한이 없습니다"); }
        return null;
    }

    /**
     * TODO: 클래스 레벨의 @RequireRole 어노테이션을 처리하는 메소드를 작성해야 합니다
     * - @Around 어노테이션으로 클래스 레벨 어노테이션 처리
     * - checkRole()과 동일한 로직이지만 클래스 레벨에서 추출
     *
     * @param joinPoint AOP Join Point
     * @return 메소드 실행 결과
     * @throws Throwable
     */
    @Around("@within(com.babgo.global.security.annotation.UserRole)")
    public Object checkRoleOnClass(ProceedingJoinPoint joinPoint) throws Throwable {
        // 구현 필요
        // 1. Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 2. if (authentication == null || !authentication.isAuthenticated()) {
        //        throw new AccessDeniedException("인증이 필요합니다");
        //    }
        // 3. Class<?> targetClass = joinPoint.getTarget().getClass();
        //    RequireRole requireRole = targetClass.getAnnotation(RequireRole.class);
        // 4. UserRole[] requiredRoles = requireRole.value();
        // 5. CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        //    UserRole userRole = userDetails.getRole();
        // 6. boolean hasRole = Arrays.asList(requiredRoles).contains(userRole);
        // 7. if (hasRole) { return joinPoint.proceed(); }
        // 8. else { throw new AccessDeniedException("접근 권한이 없습니다"); }
        return null;
    }
}