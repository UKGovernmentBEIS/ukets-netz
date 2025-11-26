package uk.gov.netz.api.workflow.request.flow.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.service.RequestService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentDetermineAmountServiceFacade {

    private final RequestService requestService;
    private final List<PaymentDetermineAmountByRequestTypeService> paymentDetermineAmountByRequestTypeServices;
    private final PaymentDetermineAmountDefaultService paymentDetermineAmountDefaultService;
    
    @Transactional
    public BigDecimal resolveAmountAndPopulateRequestPayload(String requestId) {
        final BigDecimal amount = resolveAmount(requestId);

        Request request = requestService.findRequestById(requestId);
        RequestPayload requestPayload = request.getPayload();
        requestPayload.setPaymentAmount(amount);
        return amount;
    }
    
    @Transactional
    public BigDecimal resolveAmount(String requestId) {
        Request request = requestService.findRequestById(requestId);

        Optional<PaymentDetermineAmountByRequestTypeService> byRequestTypeService = getPaymentDetermineAmountByRequestTypeService(
                request.getType());
        final PaymentDetermineAmountService determineAmountService = byRequestTypeService.isPresent()
                ? byRequestTypeService.get()
                : paymentDetermineAmountDefaultService;
        return determineAmountService.determineAmount(request);
    }
    
    private Optional<PaymentDetermineAmountByRequestTypeService> getPaymentDetermineAmountByRequestTypeService(RequestType requestType) {
        return paymentDetermineAmountByRequestTypeServices.stream()
            .filter(service -> service.getRequestType().equals(requestType.getCode()))
            .findAny();
    }
    
}
