package uk.gov.netz.api.security;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.RoleAuthorizationService;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizedRoleAspect {

    private final RoleAuthorizationService roleAuthorizationService;
    private final AuthorizationAspectUserResolver authorizationAspectUserResolver;

    @Before("@annotation(uk.gov.netz.api.security.AuthorizedRole)")
    public void authorize(JoinPoint joinPoint) {
    	String[] roleTypes = getRoleTypes(joinPoint);
        AppUser user = authorizationAspectUserResolver.getUser(joinPoint);
        if(user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        roleAuthorizationService.evaluate(user, roleTypes);
    }

    private String[] getRoleTypes(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuthorizedRole authorizedRole = method.getAnnotation(AuthorizedRole.class);
        return authorizedRole.roleType();
    }
}
