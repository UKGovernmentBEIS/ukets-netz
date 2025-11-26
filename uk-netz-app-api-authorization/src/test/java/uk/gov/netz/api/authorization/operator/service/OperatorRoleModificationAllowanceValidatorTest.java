package uk.gov.netz.api.authorization.operator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.operator.domain.AccountOperatorAuthorityUpdateDTO;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.authorization.AuthorityConstants.CONSULTANT_AGENT;
import static uk.gov.netz.api.authorization.AuthorityConstants.EMITTER_CONTACT;
import static uk.gov.netz.api.authorization.AuthorityConstants.OPERATOR_ADMIN_ROLE_CODE;
import static uk.gov.netz.api.authorization.AuthorityConstants.OPERATOR_ROLE_CODE;
import static uk.gov.netz.api.authorization.AuthorityConstants.VERIFIER_ADMIN_ROLE_CODE;

@ExtendWith(MockitoExtension.class)
class OperatorRoleModificationAllowanceValidatorTest {
    private static final Long ACCOUNT_ID = 1L;
    private static final String USER_ID = "userId";

    @InjectMocks
    private OperatorRoleModificationAllowanceValidator operatorRoleModificationAllowanceValidator;

    @Mock
    private AuthorityRepository authorityRepository;

    @ParameterizedTest
    @MethodSource("provideSuccessCases")
    void validate(String dtoRole, String databaseRole) {

        List<AccountOperatorAuthorityUpdateDTO> accountOperatorAuthorityUpdates = List.of(
                createAccountOperatorAuthorityUpdateDTO(dtoRole)
        );

        List<Authority> existingAccountOperatorAuthorities = List.of(createAuthority(databaseRole));

        when(authorityRepository.findAllByUserIdInAndAccountId(Set.of(USER_ID), ACCOUNT_ID))
                .thenReturn(existingAccountOperatorAuthorities);

        operatorRoleModificationAllowanceValidator.validateUpdate(accountOperatorAuthorityUpdates, ACCOUNT_ID);

        verify(authorityRepository).findAllByUserIdInAndAccountId(Set.of(USER_ID), ACCOUNT_ID);
        verifyNoMoreInteractions(authorityRepository);
    }

    private static Stream<Arguments> provideSuccessCases() {
        return Stream.of(
                Arguments.of(OPERATOR_ADMIN_ROLE_CODE, OPERATOR_ADMIN_ROLE_CODE),
                Arguments.of(OPERATOR_ROLE_CODE, OPERATOR_ROLE_CODE),
                Arguments.of(EMITTER_CONTACT, EMITTER_CONTACT),
                Arguments.of(CONSULTANT_AGENT, CONSULTANT_AGENT),
                Arguments.of(VERIFIER_ADMIN_ROLE_CODE, VERIFIER_ADMIN_ROLE_CODE),
                Arguments.of(OPERATOR_ROLE_CODE, OPERATOR_ADMIN_ROLE_CODE),
                Arguments.of(OPERATOR_ADMIN_ROLE_CODE, OPERATOR_ROLE_CODE)
        );
    }


    @ParameterizedTest
    @MethodSource("provideErrorCases")
    void validate_throws_business_exception(String dtoRole, String databaseRole) {
        List<AccountOperatorAuthorityUpdateDTO> accountOperatorAuthorityUpdates = List.of(
                createAccountOperatorAuthorityUpdateDTO(dtoRole)
        );

        List<Authority> existingAccountOperatorAuthorities = List.of(createAuthority(databaseRole));

        when(authorityRepository.findAllByUserIdInAndAccountId(Set.of(USER_ID), ACCOUNT_ID))
                .thenReturn(existingAccountOperatorAuthorities);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            operatorRoleModificationAllowanceValidator.validateUpdate(accountOperatorAuthorityUpdates, ACCOUNT_ID);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTHORITY_USER_ROLE_MODIFICATION_NOT_ALLOWED);
        verify(authorityRepository).findAllByUserIdInAndAccountId(Set.of(USER_ID), ACCOUNT_ID);
        verifyNoMoreInteractions(authorityRepository);
    }

    private static Stream<Arguments> provideErrorCases() {
        return Stream.of(
                Arguments.of(OPERATOR_ADMIN_ROLE_CODE, VERIFIER_ADMIN_ROLE_CODE),
                Arguments.of(VERIFIER_ADMIN_ROLE_CODE, OPERATOR_ADMIN_ROLE_CODE),
                Arguments.of(OPERATOR_ADMIN_ROLE_CODE, CONSULTANT_AGENT),
                Arguments.of(CONSULTANT_AGENT, OPERATOR_ADMIN_ROLE_CODE),
                Arguments.of(OPERATOR_ADMIN_ROLE_CODE, EMITTER_CONTACT),
                Arguments.of(EMITTER_CONTACT, OPERATOR_ADMIN_ROLE_CODE),

                Arguments.of(OPERATOR_ROLE_CODE, VERIFIER_ADMIN_ROLE_CODE),
                Arguments.of(VERIFIER_ADMIN_ROLE_CODE, OPERATOR_ROLE_CODE),
                Arguments.of(OPERATOR_ROLE_CODE, CONSULTANT_AGENT),
                Arguments.of(CONSULTANT_AGENT, OPERATOR_ROLE_CODE),
                Arguments.of(OPERATOR_ROLE_CODE, EMITTER_CONTACT),
                Arguments.of(EMITTER_CONTACT, OPERATOR_ROLE_CODE),

                Arguments.of(VERIFIER_ADMIN_ROLE_CODE, OPERATOR_ADMIN_ROLE_CODE),
                Arguments.of(OPERATOR_ADMIN_ROLE_CODE, VERIFIER_ADMIN_ROLE_CODE),
                Arguments.of(VERIFIER_ADMIN_ROLE_CODE, OPERATOR_ROLE_CODE),
                Arguments.of(OPERATOR_ROLE_CODE, VERIFIER_ADMIN_ROLE_CODE),
                Arguments.of(VERIFIER_ADMIN_ROLE_CODE, CONSULTANT_AGENT),
                Arguments.of(CONSULTANT_AGENT, VERIFIER_ADMIN_ROLE_CODE),
                Arguments.of(VERIFIER_ADMIN_ROLE_CODE, EMITTER_CONTACT),
                Arguments.of(EMITTER_CONTACT, VERIFIER_ADMIN_ROLE_CODE),

                Arguments.of(CONSULTANT_AGENT, OPERATOR_ADMIN_ROLE_CODE),
                Arguments.of(OPERATOR_ADMIN_ROLE_CODE, CONSULTANT_AGENT),
                Arguments.of(CONSULTANT_AGENT, OPERATOR_ROLE_CODE),
                Arguments.of(OPERATOR_ROLE_CODE, CONSULTANT_AGENT),
                Arguments.of(CONSULTANT_AGENT, VERIFIER_ADMIN_ROLE_CODE),
                Arguments.of(VERIFIER_ADMIN_ROLE_CODE, CONSULTANT_AGENT),
                Arguments.of(CONSULTANT_AGENT, EMITTER_CONTACT),
                Arguments.of(EMITTER_CONTACT, CONSULTANT_AGENT),

                Arguments.of(EMITTER_CONTACT, OPERATOR_ADMIN_ROLE_CODE),
                Arguments.of(OPERATOR_ADMIN_ROLE_CODE, EMITTER_CONTACT),
                Arguments.of(EMITTER_CONTACT, OPERATOR_ROLE_CODE),
                Arguments.of(OPERATOR_ROLE_CODE, EMITTER_CONTACT),
                Arguments.of(EMITTER_CONTACT, CONSULTANT_AGENT),
                Arguments.of(CONSULTANT_AGENT, EMITTER_CONTACT),
                Arguments.of(EMITTER_CONTACT, VERIFIER_ADMIN_ROLE_CODE),
                Arguments.of(VERIFIER_ADMIN_ROLE_CODE, EMITTER_CONTACT)
        );
    }


    @Test
    void validate_no_one_is_updated() {
        List<AccountOperatorAuthorityUpdateDTO> accountOperatorAuthorityUpdates = List.of(
                createAccountOperatorAuthorityUpdateDTO(OPERATOR_ADMIN_ROLE_CODE)
        );

        operatorRoleModificationAllowanceValidator.validateUpdate(accountOperatorAuthorityUpdates, ACCOUNT_ID);
        verify(authorityRepository).findAllByUserIdInAndAccountId(Set.of(USER_ID), ACCOUNT_ID);
        verifyNoMoreInteractions(authorityRepository);
    }

    private AccountOperatorAuthorityUpdateDTO createAccountOperatorAuthorityUpdateDTO(String operatorAdminRoleCode) {
        return AccountOperatorAuthorityUpdateDTO.builder()
                .userId(USER_ID)
                .roleCode(operatorAdminRoleCode)
                .build();
    }

    private Authority createAuthority(String code) {
        return Authority.builder()
                .userId(USER_ID)
                .code(code)
                .build();
    }
}