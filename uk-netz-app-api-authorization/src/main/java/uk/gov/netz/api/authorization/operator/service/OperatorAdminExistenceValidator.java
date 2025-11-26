package uk.gov.netz.api.authorization.operator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.operator.domain.AccountOperatorAuthorityUpdateDTO;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.netz.api.authorization.AuthorityConstants.OPERATOR_ADMIN_ROLE_CODE;
import static uk.gov.netz.api.authorization.core.domain.AuthorityStatus.ACTIVE;

@Component
@RequiredArgsConstructor
public class OperatorAdminExistenceValidator implements OperatorAuthorityUpdateValidator, OperatorAuthorityDeleteValidator {

    private final AuthorityRepository authorityRepository;
    private final OperatorAuthorityQueryService operatorAuthorityQueryService;

    /**
     * Checks that exist at least one user with role code OPERATOR_ADMIN and status {@link AuthorityStatus#ACTIVE}
     * for the provided {@code accountId}
     * @param accountOperatorAuthorities {@link List} of {@link AccountOperatorAuthorityUpdateDTO}
     * @param accountId the account id
     */
    @Override
    public void validateUpdate(List<AccountOperatorAuthorityUpdateDTO> accountOperatorAuthorities, Long accountId) {
        if (!isActiveOperatorAdminSelected(accountOperatorAuthorities)) {
            List<String> currentActiveOperatorAdmins = operatorAuthorityQueryService.findActiveOperatorAdminUsersByAccount(accountId);

            List<String> currentActiveOperatorAdminsToBeUpdated = accountOperatorAuthorities.stream()
                .filter(accountOperatorAuthority -> currentActiveOperatorAdmins.contains(accountOperatorAuthority.getUserId()) &&
                    (!OPERATOR_ADMIN_ROLE_CODE.equalsIgnoreCase(accountOperatorAuthority.getRoleCode()) ||
                        ACTIVE != accountOperatorAuthority.getAuthorityStatus()))
                .map(AccountOperatorAuthorityUpdateDTO::getUserId)
                .collect(Collectors.toList());

            if (currentActiveOperatorAdminsToBeUpdated.containsAll(currentActiveOperatorAdmins)) {
                throw new BusinessException(ErrorCode.AUTHORITY_MIN_ONE_OPERATOR_ADMIN_SHOULD_EXIST);
            }
        }
    }

    @Override
    public void validateDeletion(Authority authority) {
        if (OPERATOR_ADMIN_ROLE_CODE.equals(authority.getCode()) &&
            !authorityRepository.existsOtherAccountOperatorAdmin(authority.getUserId(),authority.getAccountId())) {
            throw new BusinessException(ErrorCode.AUTHORITY_MIN_ONE_OPERATOR_ADMIN_SHOULD_EXIST);
        }
    }

    private boolean isActiveOperatorAdminSelected(List<AccountOperatorAuthorityUpdateDTO> accountOperatorAuthorities) {
        return accountOperatorAuthorities.stream()
            .anyMatch(accountOperatorAuthority ->
                OPERATOR_ADMIN_ROLE_CODE.equalsIgnoreCase(accountOperatorAuthority.getRoleCode()) &&
                    ACTIVE == accountOperatorAuthority.getAuthorityStatus()
            );
    }
}
