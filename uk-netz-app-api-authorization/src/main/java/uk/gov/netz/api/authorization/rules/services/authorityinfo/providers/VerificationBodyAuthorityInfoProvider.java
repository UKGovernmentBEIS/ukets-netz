package uk.gov.netz.api.authorization.rules.services.authorityinfo.providers;


import java.util.Optional;

public interface VerificationBodyAuthorityInfoProvider {

    Optional<Long> getThirdPartyDataProviderId(Long verificationBodyId);
    
}
