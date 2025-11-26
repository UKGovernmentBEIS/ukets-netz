package uk.gov.netz.api.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.domain.dto.AccountContactInfoDTO;
import uk.gov.netz.api.account.repository.AccountBaseRepository;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;

@RequiredArgsConstructor
public abstract class ApprovedAccountQueryAbstractService<T extends Account>
		implements ApprovedAccountQueryService<T> {
	
	private final AccountBaseRepository<T> accountBaseRepository;
	
	@Override
	public List<Long> getAllApprovedAccountIdsByCa(CompetentAuthorityEnum competentAuthority) {
		return accountBaseRepository.findAccountIdsByCaAndStatusNotIn(competentAuthority,
				getStatusesConsideredNotApproved());
	}

	@Override
	public Page<AccountContactInfoDTO> getApprovedAccountsAndCaSiteContactsByCa(
			CompetentAuthorityEnum competentAuthority, Integer page, Integer pageSize) {
		return accountBaseRepository.findAccountContactsByCaAndContactTypeAndStatusNotIn(PageRequest.of(page, pageSize),
				competentAuthority, AccountContactType.CA_SITE, getStatusesConsideredNotApproved());
	}
	
	@Override
    public boolean isAccountApproved(T account) {
        return !getStatusesConsideredNotApproved().contains(account.getStatus());
    }

}
