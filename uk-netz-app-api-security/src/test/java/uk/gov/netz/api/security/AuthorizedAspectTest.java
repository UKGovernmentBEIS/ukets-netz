package uk.gov.netz.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.AppUserAuthorizationService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizedAspectTest {

    @Mock
    private AppUserAuthorizationService appUserAuthorizationService;

    @Mock
    private AppSecurityComponent appSecurityComponent;

    private AuthorizedTest authorizedTest = new AuthorizedTest();
    private static final AppUser USER = AppUser.builder().userId("userId").roleType(RoleTypeConstants.OPERATOR).build();

    @BeforeEach
    public void setUp() {
        AuthorizationAspectUserResolver authorizationAspectUserResolver = new AuthorizationAspectUserResolver(appSecurityComponent);
        AuthorizedAspect aspect = new AuthorizedAspect(appUserAuthorizationService, authorizationAspectUserResolver);

        AspectJProxyFactory aspectJProxyFactory = new AspectJProxyFactory(authorizedTest);
        aspectJProxyFactory.addAspect(aspect);

        DefaultAopProxyFactory proxyFactory = new DefaultAopProxyFactory();
        AopProxy aopProxy = proxyFactory.createAopProxy(aspectJProxyFactory);

        authorizedTest = (AuthorizedTest) aopProxy.getProxy();
    }
    
    @Test
    void authorizeWithNullAuthenticatedUser() {
    	when(appSecurityComponent.getAuthenticatedUser()).thenReturn(null);
    	BusinessException be = assertThrows(BusinessException.class, () -> authorizedTest.testMethodResourceNull(null));
    	assertThat(be.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void authorizeLong() {
        authorizedTest.testMethodResourceLong(USER, 1L);
        verify(appUserAuthorizationService, times(1)).authorize(USER, "testMethodResourceLong", "1", null, null);
    }

    @Test
    void authorizeString() {
        authorizedTest.testMethodResourceString(USER, "aaa");
        verify(appUserAuthorizationService, times(1)).authorize(USER, "testMethodResourceString", "aaa", null, null);
    }

    @Test
    void authorizeEmptyUser() {
        when(appSecurityComponent.getAuthenticatedUser()).thenReturn(USER);

        authorizedTest.testMethodResourceEmptyUser("aaa");
        verify(appUserAuthorizationService, times(1)).authorize(USER, "testMethodResourceEmptyUser", "aaa", null, null);
    }

    @Test
    void authorizeNull() {
        authorizedTest.testMethodResourceNull(USER);
        verify(appUserAuthorizationService, times(1)).authorize(USER, "testMethodResourceNull");
    }

    @Test
    void authorizeWithResourceType() {
        authorizedTest.testMethodResourceTypeString(USER, "resourceId", "resourceType");
        verify(appUserAuthorizationService, times(1)).authorize(USER, "testMethodResourceTypeString", "resourceId", "resourceType", null);
    }

    @Test
    void authorizeWithResourceSubType() {
        authorizedTest.testMethodResourceSubTypeString(USER, "resourceId", "resourceSubType");
        verify(appUserAuthorizationService, times(1)).authorize(USER, "testMethodResourceSubTypeString", "resourceId", null, "resourceSubType");
    }

    @Test
    void authorizeWithResourceTypeAndResourceSubType() {
        authorizedTest.testMethodResourceTypeResourceSubTypeString(USER, "resourceId", "resourceType", "resourceSubType");
        verify(appUserAuthorizationService, times(1)).authorize(USER, "testMethodResourceTypeResourceSubTypeString", "resourceId", "resourceType", "resourceSubType");
    }
    
    public static class AuthorizedTest {
        @Authorized(resourceId = "#resourceId")
        public void testMethodResourceLong(AppUser user, Long resourceId) {
        }

        @Authorized(resourceId = "#resourceId")
        public void testMethodResourceString(AppUser user, String resourceId) {
        }

        @Authorized(resourceId = "#resourceId")
        public void testMethodResourceNull(AppUser user) {
        }

        @Authorized(resourceId = "#resourceId")
        public void testMethodResourceEmptyUser(String resourceId) {
        }

        @Authorized(resourceId = "#resourceId", resourceType = "#resourceType")
        public void testMethodResourceTypeString(AppUser user, String resourceId, String resourceType) {
        }

        @Authorized(resourceId = "#resourceId", resourceSubType = "#resourceSubType")
        public void testMethodResourceSubTypeString(AppUser user, String resourceId, String resourceSubType) {
        }

        @Authorized(resourceId = "#resourceId", resourceType = "#resourceType", resourceSubType = "#resourceSubType")
        public void testMethodResourceTypeResourceSubTypeString(AppUser user, String resourceId, String resourceType, String resourceSubType) {
        }
    }
}