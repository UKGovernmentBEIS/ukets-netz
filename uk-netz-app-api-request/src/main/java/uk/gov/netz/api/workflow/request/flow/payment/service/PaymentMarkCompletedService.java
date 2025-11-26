package uk.gov.netz.api.workflow.request.flow.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.workflow.request.core.service.RequestService;

@Service
@RequiredArgsConstructor
public class PaymentMarkCompletedService {

    private final RequestService requestService;
    
    public void paymentCompleted(final String requestId) {
        requestService.paymentCompleted(requestId);
    }
}
