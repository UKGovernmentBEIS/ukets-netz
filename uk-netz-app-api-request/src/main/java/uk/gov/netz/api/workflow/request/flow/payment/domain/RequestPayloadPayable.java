package uk.gov.netz.api.workflow.request.flow.payment.domain;

public interface RequestPayloadPayable {

    RequestPaymentInfo getRequestPaymentInfo();

    void setRequestPaymentInfo(RequestPaymentInfo requestPaymentInfo);
}
