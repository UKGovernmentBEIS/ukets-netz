package uk.gov.netz.api.security;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.AppUserAuthorizationService;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.common.utils.SpELParser;

import java.lang.reflect.Method;

/**
 * Aspect triggered {@link Before} {@link AuthorizedAspect} annotated methods.
 * Retrieves:
 * <ul>
 *     <li>resourceId based on {@link AuthorizedAspect} parameters</li>
 *     <li>resourceSubType on {@link AuthorizedAspect} parameters </li>
 *     <li>serviceName the annotated method name</li>
 *     <li>{@link AppUser} from annotated method parameters</li>
 * </ul>
 * Calls {@link AppUserAuthorizationService} to evaluate authorization.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizedAspect {

    private final AppUserAuthorizationService appUserAuthorizationService;
    private final AuthorizationAspectUserResolver authorizationAspectUserResolver;

    @Before("@annotation(uk.gov.netz.api.security.Authorized)")
    public void authorize(JoinPoint joinPoint) {
        String serviceName = getServiceName(joinPoint);
        String resourceId = getResourceId(joinPoint);
        String resourceType = getResourceType(joinPoint);
        String resourceSubType = getResourceSubType(joinPoint);
        AppUser user = authorizationAspectUserResolver.getUser(joinPoint);
        if(user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        //the first if covers cases where a user wants to create a resource of a specific subType and as such resourceId does not exist yet (e.g Installation create account)
        if (!StringUtils.isEmpty(resourceSubType)) {
            appUserAuthorizationService.authorize(user, serviceName, resourceId, resourceType, resourceSubType);
        }
        else if (StringUtils.isEmpty(resourceId)) {
            appUserAuthorizationService.authorize(user, serviceName);
        } else {
            appUserAuthorizationService.authorize(user, serviceName, resourceId, resourceType, resourceSubType);
        }
    }

    private String getResourceId(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Authorized authorized = method.getAnnotation(Authorized.class);
        return SpELParser.parseExpression(authorized.resourceId(), signature.getParameterNames(), joinPoint.getArgs(), String.class);
    }

    private String getServiceName(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getName();
    }

    private String getResourceType(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Authorized authorized = method.getAnnotation(Authorized.class);
        return SpELParser.parseExpression(authorized.resourceType(), signature.getParameterNames(), joinPoint.getArgs(), String.class);
    }

    private String getResourceSubType(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Authorized authorized = method.getAnnotation(Authorized.class);
        return SpELParser.parseExpression(authorized.resourceSubType(), signature.getParameterNames(), joinPoint.getArgs(), String.class);
    }
}
