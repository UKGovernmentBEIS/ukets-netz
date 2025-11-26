package uk.gov.netz.api.workflow.request.flow.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.verificationbody.domain.verificationbodydetails.VerificationBodyDetails;
import uk.gov.netz.api.verificationbody.domain.verificationreport.VerificationReport;
import uk.gov.netz.api.verificationbody.service.VerificationBodyDetailsQueryService;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestVerificationServiceTest {

    @InjectMocks
    private RequestVerificationService requestVerificationService;

    @Mock
    private VerificationBodyDetailsQueryService verificationBodyDetailsQueryService;

    @Mock
    private VerificationReport verificationReport;

    @Mock
    private VerificationBodyDetails verificationBodyDetails;


    @Test
    void testRefreshVerificationReportVBDetails_withVerificationReportVBId() {
        Long vbId = 1L;

        when(verificationReport.getVerificationBodyId()).thenReturn(vbId);
        when(verificationBodyDetailsQueryService.getVerificationBodyDetails(vbId)).thenReturn(Optional.of(verificationBodyDetails));

        requestVerificationService.refreshVerificationReportVBDetails(verificationReport, 2L); // requestVBId is ignored

        verify(verificationBodyDetailsQueryService).getVerificationBodyDetails(vbId);
        verify(verificationReport).setVerificationBodyDetails(verificationBodyDetails);
    }

    @Test
    void testRefreshVerificationReportVBDetails_withNullVerificationReportVBId_usesRequestVBId() {
        Long requestVBId = 2L;

        when(verificationReport.getVerificationBodyId()).thenReturn(null);
        when(verificationBodyDetailsQueryService.getVerificationBodyDetails(requestVBId)).thenReturn(Optional.of(verificationBodyDetails));

        requestVerificationService.refreshVerificationReportVBDetails(verificationReport, requestVBId);

        verify(verificationBodyDetailsQueryService).getVerificationBodyDetails(requestVBId);
        verify(verificationReport).setVerificationBodyDetails(verificationBodyDetails);
    }

}

