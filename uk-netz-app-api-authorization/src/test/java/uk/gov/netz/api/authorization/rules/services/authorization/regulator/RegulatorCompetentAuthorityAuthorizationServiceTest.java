package uk.gov.netz.api.authorization.rules.services.authorization.regulator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Map;

class RegulatorCompetentAuthorityAuthorizationServiceTest {
    private final RegulatorCompetentAuthorityAuthorizationService regulatorCompAuthAuthorizationService = new RegulatorCompetentAuthorityAuthorizationService();

    private final AppAuthority appAuthority = AppAuthority.builder()
            .competentAuthority(CompetentAuthorityEnum.ENGLAND)
            .permissions(List.of("permission1", "permission2"))
            .build();
    private final AppUser user = AppUser.builder().authorities(List.of(appAuthority)).roleType(RoleTypeConstants.REGULATOR).build();

    @Test
    void isAuthorized_account_with_criteria_true() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
                .build();
        Assertions.assertTrue(regulatorCompAuthAuthorizationService.isAuthorized(user, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_with_criteria_with_permission_true() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
                .permission("permission1")
                .build();
        Assertions.assertTrue(regulatorCompAuthAuthorizationService.isAuthorized(user, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_with_criteria_false() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.SCOTLAND.name()))
                .build();
        Assertions.assertFalse(regulatorCompAuthAuthorizationService.isAuthorized(user, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_with_criteria_with_permission_false() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
                .permission("permission3")
                .build();
        Assertions.assertFalse(regulatorCompAuthAuthorizationService.isAuthorized(user, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_true() {
        Assertions.assertTrue(regulatorCompAuthAuthorizationService.isAuthorized(user, CompetentAuthorityEnum.ENGLAND));
    }

    @Test
    void isAuthorized_account_false() {
        Assertions.assertFalse(regulatorCompAuthAuthorizationService.isAuthorized(user, CompetentAuthorityEnum.SCOTLAND));
    }

    @Test
    void isAuthorized_account_with_permissions_true() {
        Assertions.assertTrue(regulatorCompAuthAuthorizationService.isAuthorized(user, CompetentAuthorityEnum.ENGLAND, "permission1"));
    }

    @Test
    void isAuthorized_account_with_permissions_false() {
        Assertions.assertFalse(regulatorCompAuthAuthorizationService.isAuthorized(user, CompetentAuthorityEnum.ENGLAND, "permission3"));
    }

    @Test
    void isApplicable_true() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
                .build();
        Assertions.assertTrue(regulatorCompAuthAuthorizationService.isApplicable(authorizationCriteria));
    }

    @Test
    void isApplicable_false() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .build();
        Assertions.assertFalse(regulatorCompAuthAuthorizationService.isApplicable(authorizationCriteria));
    }
}