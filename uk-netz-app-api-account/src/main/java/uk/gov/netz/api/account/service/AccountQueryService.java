package uk.gov.netz.api.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountInfoDTO;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;
import uk.gov.netz.api.account.repository.AccountRepository;
import uk.gov.netz.api.account.transform.AccountMapper;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.AccountAuthorityInfoProvider;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.netz.api.common.exception.ErrorCode.RESOURCE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AccountQueryService implements AccountAuthorityInfoProvider {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public CompetentAuthorityEnum getAccountCa(Long accountId) {
        return getAccountById(accountId).getCompetentAuthority();
    }

    public String getAccountName(Long accountId) {
        return getAccountById(accountId).getName();
    }

    public AccountStatus getAccountStatus(Long accountId) {
        return getAccountById(accountId).getStatus();
    }

    public Account exclusiveLockAccount(final Long accountId) {
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND));
    }

    @Override
    public Optional<Long> getAccountVerificationBodyId(Long accountId) {
        return Optional.ofNullable(getAccountById(accountId).getVerificationBodyId());
    }
    
    @Override
	public Set<Long> findAccountIdsByVerificationBodyId(Long verificationBodyId) {
		return new HashSet<>(accountRepository.findAllIdsByVerificationBody(verificationBodyId));
	}

    public AccountInfoDTO getAccountInfoDTOById(Long accountId) {
        return accountMapper.toAccountInfoDTO(getAccountById(accountId));
    }

    public Set<Long> getAccountIds(List<Long> accountIds) {
        return accountRepository.findAllByIdIn(accountIds)
            .stream()
            .map(Account::getId)
            .collect(Collectors.toSet());
    }

    public List<Account> getAccounts(List<Long> accountIds) {
        return accountRepository.findAllByIdIn(accountIds);
    }
    
    @Override
    public Set<CompetentAuthorityEnum> findCAByIdIn(Set<Long> accountIds){
    	return accountRepository.findCAByIdIn(accountIds);
    }

    Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND));
    }

	@Override
	public Optional<Long> getThirdPartyDataProviderId(Long accountId) {
		return Optional.ofNullable(getAccountById(accountId).getThirdPartyDataProviderId());
	}

}
