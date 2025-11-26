package uk.gov.netz.api.authorization.operator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.operator.domain.AccountOperatorAuthorityUpdateDTO;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.netz.api.authorization.AuthorityConstants.OPERATOR_ADMIN_ROLE_CODE;
import static uk.gov.netz.api.authorization.AuthorityConstants.OPERATOR_ROLE_CODE;

@Component
@RequiredArgsConstructor
public class OperatorRoleModificationAllowanceValidator implements OperatorAuthorityUpdateValidator {

    private final AuthorityRepository authorityRepository;

    private static final List<String> ALLOWED_TRANSITION_ROLE_CODES =
            List.of(OPERATOR_ADMIN_ROLE_CODE, OPERATOR_ROLE_CODE);

    /**
     * Checks that existing operator users of the {@code accountId } with specific role codes do not change roles.
     * The only valid role transition is from operator to operator admin and vice versa.
     * @param accountOperatorAuthorities accountOperatorAuthorities {@link List} of {@link AccountOperatorAuthorityUpdateDTO}
     * @param accountId the account id
     */
    @Override
    public void validateUpdate(List<AccountOperatorAuthorityUpdateDTO> accountOperatorAuthorities, Long accountId) {
        Set<String> userIds = accountOperatorAuthorities.stream()
                .map(AccountOperatorAuthorityUpdateDTO::getUserId)
                .collect(Collectors.toSet());

        Map<String, String> initialRoleCodes = getAccountUsers(userIds, accountId);

        accountOperatorAuthorities.forEach(authorityUpdate -> {
            String userId = authorityUpdate.getUserId();
            String updatedRoleCode = authorityUpdate.getRoleCode();
            String initialRoleCode = initialRoleCodes.get(userId);

            boolean hasUpdatedRoleCode = !updatedRoleCode.equals(initialRoleCode);
            boolean isForbiddenTransition = initialRoleCode != null
                    && (!ALLOWED_TRANSITION_ROLE_CODES.contains(initialRoleCode)
                    || !ALLOWED_TRANSITION_ROLE_CODES.contains(updatedRoleCode));

            if (hasUpdatedRoleCode && isForbiddenTransition) {
                throw new BusinessException(ErrorCode.AUTHORITY_USER_ROLE_MODIFICATION_NOT_ALLOWED, userId, updatedRoleCode);
            }
        });
    }

    private Map<String, String> getAccountUsers(Set<String> userIds, Long accountId) {
        return authorityRepository.findAllByUserIdInAndAccountId(userIds, accountId).stream()
                .collect(Collectors.toMap(Authority::getUserId, Authority::getCode));
    }
}
