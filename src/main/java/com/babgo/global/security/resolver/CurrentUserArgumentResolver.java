package com.babgo.global.security.resolver;

import com.babgo.application.user.UserDetailInfo;
import com.babgo.domain.user.User;
import com.babgo.global.exception.CustomException;
import com.babgo.global.exception.ErrorCode;
import com.babgo.global.security.annotation.CurrentUser;
import com.babgo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @CurrentUser 어노테이션이 붙은 파라미터에 현재 인증된 사용자 정보를 주입하는 Resolver
 *
 * 지원하는 파라미터 타입:
 * 1. User - User 엔티티 전체
 * 2. Long - userId (PK)
 * 3. String - email
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserRepository userRepository;

    /**
     * 이 Resolver가 처리할 파라미터인지 판단
     * @CurrentUser 어노테이션이 붙어있으면 true
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    /**
     * 실제로 파라미터에 주입할 값을 반환
     */
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {
        // 1. SecurityContext에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증되지 않은 경우 예외 처리
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증되지 않은 사용자가 @CurrentUser 파라미터에 접근 시도");
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        // 3. Principal이 UserDetailInfo인지 확인
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailInfo)) {
            log.warn("Principal이 UserDetailInfo가 아닙니다: {}", principal.getClass().getName());
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        UserDetailInfo userDetailInfo = (UserDetailInfo) principal;

        // 4. userId로 User 엔티티 조회
        Long userId = Long.parseLong(userDetailInfo.getUserId());
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> {
                    log.error("인증된 userId로 User를 찾을 수 없음: {}", userId);
                    return new CustomException(ErrorCode.USER_NOT_FOUND);
                });

        // 5. 파라미터 타입에 따라 적절한 값 반환
        Class<?> parameterType = parameter.getParameterType();

        if (parameterType.equals(User.class)) {
            // User 객체 전체를 반환
            log.debug("@CurrentUser User 주입 - userId: {}", userId);
            return user;

        } else if (parameterType.equals(Long.class)) {
            // userId (PK)만 반환
            log.debug("@CurrentUser Long 주입 - userId: {}", userId);
            return user.getUserId();

        } else if (parameterType.equals(String.class)) {
            // email을 반환
            log.debug("@CurrentUser String 주입 - email: {}", user.getEmail());
            return user.getEmail();

        } else {
            // 지원하지 않는 타입
            log.error("@CurrentUser가 지원하지 않는 타입: {}", parameterType.getName());
            throw new IllegalArgumentException(
                    "@CurrentUser는 User, Long, String 타입만 지원합니다. 현재 타입: " + parameterType.getName()
            );
        }
    }
}