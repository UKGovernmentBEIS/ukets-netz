package uk.gov.netz.api.verificationbody.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.verificationbody.domain.VerificationBody;
import uk.gov.netz.api.verificationbody.enumeration.VerificationBodyStatus;
import uk.gov.netz.api.verificationbody.repository.VerificationBodyRepository;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationBodyManagementServiceTest {

    @InjectMocks
    private VerificationBodyManagementService verificationBodyManagementService;

    @Mock
    private VerificationBodyRepository verificationBodyRepository;

    @Test
    void activateVerificationBody() {
        Long verificationBodyId = 1L;
        VerificationBody vb = VerificationBody.builder().id(verificationBodyId).status(VerificationBodyStatus.PENDING).build();
        when(verificationBodyRepository.findByIdAndStatus(verificationBodyId, VerificationBodyStatus.PENDING)).thenReturn(Optional.of(vb));

        verificationBodyManagementService.activateVerificationBody(verificationBodyId);

        verify(verificationBodyRepository, times(1)).findByIdAndStatus(verificationBodyId, VerificationBodyStatus.PENDING);

    }
}
