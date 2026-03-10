package uk.gov.netz.api.verificationbody.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.verificationbody.domain.verificationbodydetails.VerificationBodyDetails;
import uk.gov.netz.api.verificationbody.transform.VerificationBodyMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationBodyDetailsQueryService {

    private final VerificationBodyQueryService verificationBodyQueryService;
    private static final VerificationBodyMapper verificationBodyMapper = Mappers
            .getMapper(VerificationBodyMapper.class);

    public Optional<VerificationBodyDetails> getVerificationBodyDetails(Long vbId) {
        return verificationBodyQueryService.findVerificationBodyById(vbId)
                .map(verificationBodyMapper::toVerificationBodyDetails);
    }
}
