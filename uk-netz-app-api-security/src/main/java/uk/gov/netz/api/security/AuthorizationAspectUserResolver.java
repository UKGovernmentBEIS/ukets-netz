package uk.gov.netz.api.security;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.authorization.core.domain.AppUser;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthorizationAspectUserResolver {

    private final AppSecurityComponentProvider appSecurityComponent;

    public AppUser getUser(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        List<Parameter> parameterList = Arrays.asList(method.getParameters());
        return parameterList.stream()
            .filter(parameter -> parameter.getType().equals(AppUser.class))
            .findAny()
            .map(param -> (AppUser) Arrays.asList(joinPoint.getArgs()).get(parameterList.indexOf(param)))
            .orElseGet(appSecurityComponent::getAuthenticatedUser);
    }
}
