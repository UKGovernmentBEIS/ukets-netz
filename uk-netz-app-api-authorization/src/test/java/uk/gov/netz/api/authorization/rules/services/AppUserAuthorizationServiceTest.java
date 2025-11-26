package uk.gov.netz.api.authorization.rules.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppUserAuthorizationServiceTest {

    @InjectMocks
    private AppUserAuthorizationService appUserAuthorizationService;

    @Mock
    private AuthorizationRulesService authorizationRulesService;

    @Test
    void authorize_no_resource() {
        String serviceName = "serviceName";
        AppUser appUser = AppUser.builder().build();
        List<AppAuthority> authorities = List.of(
            AppAuthority.builder().accountId(1L).build()
        );
        appUser.setAuthorities(authorities);

        assertDoesNotThrow(() -> appUserAuthorizationService.authorize(appUser, serviceName));

        verify(authorizationRulesService, times(1)).evaluateRules(appUser, serviceName);
    }

    @Test
    void authorize_no_resourceType_resourceSubType() {
        String serviceName = "serviceName";
        String resourceId = "resourceId";
        AppUser appUser = AppUser.builder().build();
        List<AppAuthority> authorities = List.of(
            AppAuthority.builder().accountId(1L).build()
        );
        appUser.setAuthorities(authorities);

        appUserAuthorizationService.authorize(appUser, serviceName, resourceId, null, null);

        verify(authorizationRulesService, times(1)).evaluateRules(appUser, serviceName, resourceId, null, null);
    }

    @Test
    void authorize_with_resource_no_resourceType() {
        String serviceName = "serviceName";
        String resourceId = "resourceId";
        String resourceSubType = "resourceSubType";

        AppUser appUser = AppUser.builder().build();
        List<AppAuthority> authorities = List.of(
                AppAuthority.builder().accountId(1L).build()
        );
        appUser.setAuthorities(authorities);

        appUserAuthorizationService.authorize(appUser, serviceName, resourceId, null, resourceSubType);

        verify(authorizationRulesService, times(1))
                .evaluateRules(appUser, serviceName, resourceId, null, resourceSubType);
    }

    @Test
    void authorize_with_resource_no_resourceSubType() {
        String serviceName = "serviceName";
        String resourceId = "resourceId";
        String resourceType = "resourceType";

        AppUser appUser = AppUser.builder().build();
        List<AppAuthority> authorities = List.of(
                AppAuthority.builder().accountId(1L).build()
        );
        appUser.setAuthorities(authorities);

        appUserAuthorizationService.authorize(appUser, serviceName, resourceId, resourceType, null);

        verify(authorizationRulesService, times(1))
                .evaluateRules(appUser, serviceName, resourceId, resourceType, null);
    }

    @Test
    void authorize_with_resource_resourceType_resourceSubType() {
        String serviceName = "serviceName";
        String resourceId = "resourceId";
        String resourceType = "resourceType";
        String resourceSubType = "resourceSubType";

        AppUser appUser = AppUser.builder().build();
        List<AppAuthority> authorities = List.of(
            AppAuthority.builder().accountId(1L).build()
        );
        appUser.setAuthorities(authorities);

        appUserAuthorizationService.authorize(appUser, serviceName, resourceId, resourceType, resourceSubType);

        verify(authorizationRulesService, times(1))
            .evaluateRules(appUser, serviceName, resourceId, resourceType, resourceSubType);
    }
    
    @Test
    void authorize_installation_create_request_action() {
        String serviceName = "serviceName";
        String resourceId = null;
        String resourceType = "resourceType";
        String resourceSubType = "requestType";

        AppUser appUser = AppUser.builder().userId("user").build();

        appUserAuthorizationService.authorize(appUser, serviceName, resourceId, resourceType, resourceSubType);

        verify(authorizationRulesService, times(1))
            .evaluateRules(appUser, serviceName, resourceId, resourceType, resourceSubType);
    }

}