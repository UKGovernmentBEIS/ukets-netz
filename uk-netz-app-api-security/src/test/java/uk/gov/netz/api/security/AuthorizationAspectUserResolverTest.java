package uk.gov.netz.api.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationAspectUserResolverTest {

    @InjectMocks
    private AuthorizationAspectUserResolver authorizationAspectUserResolver;

    @Mock
    private AppSecurityComponent appSecurityComponent;

    @Test
    void getUser_when_provided_in_parameters() throws NoSuchMethodException {
        JoinPoint joinPoint = Mockito.mock(JoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        AppUser user = AppUser.builder().userId("userId").roleType(RoleTypeConstants.OPERATOR).build();

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{user});
        when(signature.getMethod()).thenReturn(getMethod("testMethodWithParam"));

        AppUser expectedUser = authorizationAspectUserResolver.getUser(joinPoint);

        assertEquals(expectedUser, user);

        verify(appSecurityComponent, never()).getAuthenticatedUser();
    }

    @Test
    void getUser_when_no_provided_in_parameters() throws NoSuchMethodException {
        JoinPoint joinPoint = Mockito.mock(JoinPoint.class);
        MethodSignature signature = Mockito.mock(MethodSignature.class);
        AppUser user = AppUser.builder().userId("userId").roleType(RoleTypeConstants.OPERATOR).build();

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(getMethod("testMethod"));
        when(appSecurityComponent.getAuthenticatedUser()).thenReturn(user);

        AppUser expectedUser = authorizationAspectUserResolver.getUser(joinPoint);

        assertEquals(expectedUser, user);

        verify(appSecurityComponent, times(1)).getAuthenticatedUser();
    }

    private Method getMethod(String methodName) throws NoSuchMethodException {
        return Arrays.stream(getClass().getDeclaredMethods())
            .filter(method -> methodName.equalsIgnoreCase(method.getName()))
            .findAny()
            .orElseThrow(NoSuchMethodException::new);
    }

    private void testMethod() {}

    private void testMethodWithParam(AppUser appUser){}
}