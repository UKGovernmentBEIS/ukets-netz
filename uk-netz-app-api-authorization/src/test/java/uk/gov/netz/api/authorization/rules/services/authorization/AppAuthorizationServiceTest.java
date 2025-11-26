package uk.gov.netz.api.authorization.rules.services.authorization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AppAuthorizationServiceTest {
    @InjectMocks
    private AppAuthorizationService appAuthorizationService;

    @Mock
    private RoleTypeAuthorizationServiceDelegator roleTypeAuthorizationServiceDelegator;

    @Test
    void isAuthorized_true() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR).build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(roleTypeAuthorizationServiceDelegator.isAuthorized(user, criteria))
                .thenReturn(true);

        appAuthorizationService.authorize(user, criteria);

        verify(roleTypeAuthorizationServiceDelegator, times(1))
                .isAuthorized(user, criteria);
    }

    @Test
    void isAuthorized_false() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR).build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(roleTypeAuthorizationServiceDelegator.isAuthorized(user, criteria))
                .thenReturn(false);

        BusinessException businessException = assertThrows(BusinessException.class,
                () -> appAuthorizationService.authorize(user, criteria));

        Assertions.assertEquals(ErrorCode.FORBIDDEN, businessException.getErrorCode());
        verify(roleTypeAuthorizationServiceDelegator, times(1))
                .isAuthorized(user, criteria);
    }
}