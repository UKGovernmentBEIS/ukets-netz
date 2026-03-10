package uk.gov.netz.api.verificationbody.service.thirdpartydataprovider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.verificationbody.domain.VerificationBody;
import uk.gov.netz.api.verificationbody.enumeration.VerificationBodyStatus;
import uk.gov.netz.api.verificationbody.service.VerificationBodyQueryService;

@RequiredArgsConstructor
@Service
public class VerificationBodyThirdPartyDataProviderUnappointService {
    
    private final VerificationBodyQueryService verificationBodyQueryService;

    @Transactional
    public void unappointThirdPartyDataProviderFromVerificationBody(Long verificationBodyId) {
        VerificationBody verificationBody = verificationBodyQueryService.getVerificationBodyById(verificationBodyId);;

        if (!VerificationBodyStatus.ACTIVE.equals(verificationBody.getStatus())) {
            throw new BusinessException(ErrorCode.VERIFICATION_BODY_INVALID_STATUS);
        }

        verificationBody.setThirdPartyDataProviderId(null);
    }
}
