package uk.gov.netz.api.workflow.request.flow.payment.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentCancelledRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentConfirmRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentProcessedRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.RequestPaymentInfo;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface PaymentPayloadMapper {

    @Mapping(target = "paidByFullName", source = "requestPaymentInfo.paidByFullName")
    @Mapping(target = "paymentDate", source = "requestPaymentInfo.paymentDate")
    @Mapping(target = "amount", source = "requestPaymentInfo.amount")
    @Mapping(target = "status", source = "requestPaymentInfo.status")
    @Mapping(target = "paymentMethod", source = "requestPaymentInfo.paymentMethod")
    @Mapping(target = "receivedDate", source = "requestPaymentInfo.receivedDate")
    PaymentProcessedRequestActionPayload toPaymentProcessedRequestActionPayload(String paymentRefNum,
                                                                                RequestPaymentInfo requestPaymentInfo,
                                                                                String payloadType);

    @Mapping(target = "payloadType", expression = "java(uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionPayloadTypes.PAYMENT_CANCELLED_PAYLOAD)")
    PaymentCancelledRequestActionPayload toPaymentCancelledRequestActionPayload(RequestPaymentInfo requestPaymentInfo);

    @Mapping(target = "payloadType", expression = "java(uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskPayloadTypes.PAYMENT_CONFIRM_PAYLOAD)")
    PaymentConfirmRequestTaskPayload toConfirmPaymentRequestTaskPayload(String paymentRefNum, RequestPaymentInfo requestPaymentInfo);
}
