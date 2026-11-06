package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    // 1. 배달 대상 검증 메서드
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라미터에 @Auth 어노테이션이 붙어있고, 타입이 AuthUser 클래스인지 검사합니다.
        boolean hasAuthAnnotation = parameter.hasParameterAnnotation(Auth.class);
        boolean isAuthUserType = parameter.getParameterType().equals(AuthUser.class);

        return hasAuthAnnotation && isAuthUserType;
    }

    // 2. 실제 유저 객체를 가공해서 배달(주입)해주는 메서드
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory
    ) throws Exception {

        // NativeWebRequest를 순수 HttpServletRequest로 캐스팅(변환)합니다.
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        // Filter(필터) 단계에서 인증 후 request 속성에 담아둔 유저 데이터를 꺼냅니다.
        Long userId = (Long) request.getAttribute("userId");
        String email = (String) request.getAttribute("email");
        UserRole userRole = (UserRole) request.getAttribute("userRole");

        // 만약 필터를 거치지 않았거나 유저 정보가 유실되었다면 인증 예외를 던집니다.
        if (userId == null || email == null || userRole == null) {
            throw new AuthException("인증 정보가 올바르지 않습니다.");
        }

        // 최종적으로 가공된 AuthUser 객체를 컨트롤러 파라미터에 쏙 넣어줍니다.
        return new AuthUser(userId, email, userRole);
    }
}
