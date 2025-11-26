package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.verificationbody.domain.verificationreport.VerificationReport;
import uk.gov.netz.api.verificationbody.service.VerificationBodyDetailsQueryService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestVerificationService {

    private final VerificationBodyDetailsQueryService verificationBodyDetailsQueryService;

    @Transactional
    public void refreshVerificationReportVBDetails(VerificationReport verificationReport, Long requestVBId) {
        if (verificationReport == null) {
            return;
        }

        final Long verificationReportVBId = verificationReport.getVerificationBodyId();
        verificationBodyDetailsQueryService
                .getVerificationBodyDetails(verificationReportVBId != null ? verificationReportVBId : requestVBId)
                .ifPresent(verificationReport::setVerificationBodyDetails);
    }
}
