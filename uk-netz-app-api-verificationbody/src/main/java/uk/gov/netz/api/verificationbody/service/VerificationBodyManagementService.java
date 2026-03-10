package uk.gov.netz.api.verificationbody.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.verificationbody.enumeration.VerificationBodyStatus;
import uk.gov.netz.api.verificationbody.repository.VerificationBodyRepository;

@Service
@RequiredArgsConstructor
public class VerificationBodyManagementService {

    private final VerificationBodyRepository verificationBodyRepository;

    public void activateVerificationBody(Long verificationBodyId) {
        verificationBodyRepository.findByIdAndStatus(verificationBodyId, VerificationBodyStatus.PENDING)
                .ifPresent(vb -> vb.setStatus(VerificationBodyStatus.ACTIVE));
    }
}
