package uk.gov.netz.api.workflow.request.flow.rfi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestService;

@Service
@RequiredArgsConstructor
public class RfiExpiredService {

    private final RequestService requestService;

    public void expire(final String requestId) {

        final Request request = requestService.findRequestById(requestId);

        requestService.addActionToRequest(request,
            null,
            RequestActionTypes.RFI_EXPIRED,
            null);
    }
}
