package uk.gov.netz.api.workflow.request.flow.payment.service;

import uk.gov.netz.api.workflow.request.core.domain.RequestType;

public interface PaymentDetermineAmountByRequestTypeService extends PaymentDetermineAmountService {

    String getRequestType();
    
}
