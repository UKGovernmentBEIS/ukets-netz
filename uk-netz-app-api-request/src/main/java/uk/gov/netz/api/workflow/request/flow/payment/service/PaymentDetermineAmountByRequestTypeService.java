package uk.gov.netz.api.workflow.request.flow.payment.service;

public interface PaymentDetermineAmountByRequestTypeService extends PaymentDetermineAmountService {

    String getRequestType();
    
}
