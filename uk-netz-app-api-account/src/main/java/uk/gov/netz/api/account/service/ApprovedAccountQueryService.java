package uk.gov.netz.api.account.service;

import org.springframework.data.domain.Page;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountContactInfoDTO;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Set;

public interface ApprovedAccountQueryService<T extends Account> {

    List<Long> getAllApprovedAccountIdsByCa(CompetentAuthorityEnum competentAuthority);

    Page<AccountContactInfoDTO> getApprovedAccountsAndCaSiteContactsByCa(CompetentAuthorityEnum competentAuthority, Integer page, Integer pageSize);

    boolean isAccountApproved(T account);
    
    Set<AccountStatus> getStatusesConsideredNotApproved();
}
