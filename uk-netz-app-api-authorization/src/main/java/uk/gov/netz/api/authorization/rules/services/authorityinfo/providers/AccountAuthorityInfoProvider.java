package uk.gov.netz.api.authorization.rules.services.authorityinfo.providers;

import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Optional;
import java.util.Set;

public interface AccountAuthorityInfoProvider {
	
    CompetentAuthorityEnum getAccountCa(Long accountId);

    Set<CompetentAuthorityEnum> findCAByIdIn(Set<Long> accountIds);
    
    Optional<Long> getAccountVerificationBodyId(Long accountId);
    
    Set<Long> findAccountIdsByVerificationBodyId(Long verificationBodyId);
    
    Optional<Long> getThirdPartyDataProviderId(Long accountId);
    
}
