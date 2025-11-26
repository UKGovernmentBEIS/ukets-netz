package uk.gov.netz.api.workflow.request.flow.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.payment.domain.enumeration.PaymentMethodType;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestActionUserInfoResolver;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentMakeRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentStatus;
import uk.gov.netz.api.workflow.request.flow.payment.domain.RequestPayloadPayable;
import uk.gov.netz.api.workflow.request.flow.payment.domain.RequestPaymentInfo;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PaymentCompleteService {

    private final RequestActionUserInfoResolver requestActionUserInfoResolver;

    @Transactional
    public void markAsPaid(RequestTask requestTask, AppUser authUser) {
        PaymentMakeRequestTaskPayload requestTaskPayload = (PaymentMakeRequestTaskPayload) requestTask.getPayload();
        validatePaymentMethod(requestTaskPayload);

        String authUserId = authUser.getUserId();
        Request request = requestTask.getRequest();
        RequestPayloadPayable requestPayloadPayable = (RequestPayloadPayable) request.getPayload();
        RequestPaymentInfo requestPaymentInfo = RequestPaymentInfo.builder()
            .paymentDate(LocalDate.now())
            .paidById(authUserId)
            .paidByFullName(requestActionUserInfoResolver.getUserFullName(authUserId))
            .amount(request.getPayload().getPaymentAmount())
            .status(PaymentStatus.MARK_AS_PAID)
            .paymentMethod(PaymentMethodType.BANK_TRANSFER)
            .build();
        requestPayloadPayable.setRequestPaymentInfo(requestPaymentInfo);

    }

    @Transactional
    public void markAsReceived(Request request, LocalDate receivedDate) {
        RequestPayload requestPayload = request.getPayload();
        RequestPayloadPayable requestPayloadPayable = (RequestPayloadPayable) requestPayload;

        String operatorAssigneeId = requestPayload.getOperatorAssignee();
        RequestPaymentInfo requestPaymentInfo = requestPayloadPayable.getRequestPaymentInfo();
        if (requestPaymentInfo == null) {
            requestPaymentInfo = RequestPaymentInfo.builder()
                .paymentDate(LocalDate.now())
                .paidById(requestPayload.getOperatorAssignee())
                .paidByFullName(requestActionUserInfoResolver.getUserFullName(operatorAssigneeId))
                .amount(requestPayload.getPaymentAmount())
                .paymentMethod(PaymentMethodType.BANK_TRANSFER)
                .build();
            requestPayloadPayable.setRequestPaymentInfo(requestPaymentInfo);
        }

        requestPaymentInfo.setReceivedDate(receivedDate);
        requestPaymentInfo.setStatus(PaymentStatus.MARK_AS_RECEIVED);
    }

    @Transactional
    public void cancel(Request request, String cancellationReason) {
        RequestPayloadPayable requestPayloadPayable = (RequestPayloadPayable) request.getPayload();

        RequestPaymentInfo requestPaymentInfo = requestPayloadPayable.getRequestPaymentInfo();
        if (requestPaymentInfo == null) {
            requestPaymentInfo = new RequestPaymentInfo();
            requestPayloadPayable.setRequestPaymentInfo(requestPaymentInfo);
        }

        requestPaymentInfo.setCancellationReason(cancellationReason);
        requestPaymentInfo.setStatus(PaymentStatus.CANCELLED);
    }

    @Transactional
    public void complete(Request request, AppUser authUser) {
        String authUserId = authUser.getUserId();
        RequestPayloadPayable requestPayloadPayable = (RequestPayloadPayable) request.getPayload();
        RequestPaymentInfo requestPaymentInfo = RequestPaymentInfo.builder()
            .paymentDate(LocalDate.now())
            .paidById(authUserId)
            .paidByFullName(requestActionUserInfoResolver.getUserFullName(authUserId))
            .amount(request.getPayload().getPaymentAmount())
            .status(PaymentStatus.COMPLETED)
            .paymentMethod(PaymentMethodType.CREDIT_OR_DEBIT_CARD)
            .build();
        requestPayloadPayable.setRequestPaymentInfo(requestPaymentInfo);
    }

    private void validatePaymentMethod(PaymentMakeRequestTaskPayload requestTaskPayload) {
        if (!requestTaskPayload.getPaymentMethodTypes().contains(PaymentMethodType.BANK_TRANSFER)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
    }
}
