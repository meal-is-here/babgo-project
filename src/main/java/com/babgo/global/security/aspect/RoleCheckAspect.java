package com.babgo.global.security.aspect;

import com.babgo.domain.user.UserRole;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.global.security.annotation.RequireRole;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * RequireRole 어노테이션을 처리하는 AOP Aspect
 * 메소드 실행 전에 사용자의 권한을 체크합니다.
 * 권한이 없으면 CustomException을 발생시킵니다.
 */
@Slf4j
@Aspect
@Component
public class RoleCheckAspect {

    /**
     * RequireRole 어노테이션이 붙은 메소드의 권한을 체크합니다
     */
    @Around("@annotation(com.babgo.global.security.annotation.RequireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. SecurityContextHolder에서 현재 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증되지 않았거나 익명 사용자인 경우 예외 발생
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            log.warn("인증되지 않은 사용자의 접근 시도");
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 3. 메소드의 @RequireRole 어노테이션 추출
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);

        // 4. 어노테이션에 정의된 필요 권한(UserRole[]) 추출
        UserRole[] requiredRoles = requireRole.value();

        // 5. 현재 사용자의 권한 추출
        String currentUserRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN));

        // 6. 필요 권한 목록에 현재 사용자 권한이 포함되는지 확인
        boolean hasRequiredRole = Arrays.stream(requiredRoles)
                .anyMatch(role -> role.getKey().equals(currentUserRole));

        // 7. 권한이 없으면 AccessDeniedException 발생
        if (!hasRequiredRole) {
            log.warn("권한 없는 접근 시도 - 필요 권한: {}, 현재 권한: {}",
                    Arrays.toString(requiredRoles), currentUserRole);
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 8. 권한이 있으면 메소드 실행
        log.debug("권한 체크 통과 - 메소드: {}, 권한: {}", method.getName(), currentUserRole);
        return joinPoint.proceed();
    }

    /**
     * 클래스 레벨의 @RequireRole 어노테이션을 처리합니다
     */
    @Around("@within(com.babgo.global.security.annotation.RequireRole)")
    public Object checkRoleOnClass(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. SecurityContextHolder에서 현재 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증되지 않았거나 익명 사용자인 경우 예외 발생
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal().equals("anonymousUser")) {
            log.warn("인증되지 않은 사용자의 접근 시도");
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 3. 클래스의 @RequireRole 어노테이션 추출
        Class<?> targetClass = joinPoint.getTarget().getClass();
        RequireRole requireRole = targetClass.getAnnotation(RequireRole.class);

        // 4. 어노테이션에 정의된 필요 권한(UserRole[]) 추출
        UserRole[] requiredRoles = requireRole.value();

        // 5. 현재 사용자의 권한 추출
        String currentUserRole = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElseThrow(() -> new CustomException(ErrorCode.FORBIDDEN));

        // 6. 필요 권한 목록에 현재 사용자 권한이 포함되는지 확인
        boolean hasRequiredRole = Arrays.stream(requiredRoles)
                .anyMatch(role -> role.getKey().equals(currentUserRole));

        // 7. 권한이 없으면 AccessDeniedException 발생
        if (!hasRequiredRole) {
            log.warn("권한 없는 접근 시도 - 필요 권한: {}, 현재 권한: {}",
                    Arrays.toString(requiredRoles), currentUserRole);
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 8. 권한이 있으면 메소드 실행
        log.debug("권한 체크 통과 - 클래스: {}, 권한: {}", targetClass.getSimpleName(), currentUserRole);
        return joinPoint.proceed();
    }
}