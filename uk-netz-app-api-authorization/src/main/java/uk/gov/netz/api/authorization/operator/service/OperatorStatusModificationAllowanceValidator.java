package uk.gov.netz.api.authorization.operator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.operator.domain.AccountOperatorAuthorityUpdateDTO;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OperatorStatusModificationAllowanceValidator implements OperatorAuthorityUpdateValidator {

    private final AuthorityRepository authorityRepository;

    @Override
    public void validateUpdate(List<AccountOperatorAuthorityUpdateDTO> accountOperatorAuthorities, Long accountId) {
        List<String> userIds = accountOperatorAuthorities.stream().map(AccountOperatorAuthorityUpdateDTO::getUserId)
                .collect(Collectors.toList());
        Map<String, AuthorityStatus> userStatuses = authorityRepository.findStatusByUsersAndAccountId(userIds, accountId);

        accountOperatorAuthorities.forEach(accountOperator -> {
            if (userStatuses.containsKey(accountOperator.getUserId())
                    && userStatuses.get(accountOperator.getUserId()).equals(AuthorityStatus.ACCEPTED)
                    && !(accountOperator.getAuthorityStatus().equals(AuthorityStatus.ACTIVE)
                        || accountOperator.getAuthorityStatus().equals(AuthorityStatus.ACCEPTED))) {

                throw new BusinessException(ErrorCode.AUTHORITY_INVALID_STATUS,
                        accountOperator.getUserId(), accountOperator.getAuthorityStatus().name());
            }
        });
    }
}
