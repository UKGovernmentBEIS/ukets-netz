package uk.gov.netz.api.authorization.rules.services;

import org.junit.jupiter.api.Test;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleAuthorizationServiceTest {

    private final RoleAuthorizationService roleAuthorizationService = new RoleAuthorizationService();

    @Test
    void evaluate() {
        AppUser operatorUser = AppUser.builder().userId("userId").roleType(RoleTypeConstants.OPERATOR).build();
        List<AppAuthority> authorities = List.of(
            AppAuthority.builder().accountId(1L).build()
        );
        operatorUser.setAuthorities(authorities);

        assertDoesNotThrow(() -> roleAuthorizationService.evaluate(operatorUser, new String[] {RoleTypeConstants.OPERATOR}));
    }

    @Test
    void evaluate_throws_business_exception_if_required_role_type_different() {
        AppUser operatorUser = AppUser.builder().userId("userId").roleType(RoleTypeConstants.OPERATOR).build();

        BusinessException businessException = assertThrows(BusinessException.class,
            () -> roleAuthorizationService.evaluate(operatorUser, new String[] {RoleTypeConstants.REGULATOR}));

        assertEquals(ErrorCode.FORBIDDEN, businessException.getErrorCode());
    }

    @Test
    void evaluate_throws_business_exception_if_user_has_no_authorities() {
        AppUser operatorUser = AppUser.builder().userId("userId").roleType(RoleTypeConstants.REGULATOR).build();

        BusinessException businessException = assertThrows(BusinessException.class,
            () -> roleAuthorizationService.evaluate(operatorUser, new String[] {RoleTypeConstants.REGULATOR}));

        assertEquals(ErrorCode.FORBIDDEN, businessException.getErrorCode());
    }

    @Test
    void evaluate_throws_business_exception_if_permitted_role_types_empty() {
        AppUser operatorUser = AppUser.builder().userId("userId").roleType(RoleTypeConstants.REGULATOR).build();

        BusinessException businessException = assertThrows(BusinessException.class,
            () -> roleAuthorizationService.evaluate(operatorUser, new String[] {}));

        assertEquals(ErrorCode.FORBIDDEN, businessException.getErrorCode());
    }
}