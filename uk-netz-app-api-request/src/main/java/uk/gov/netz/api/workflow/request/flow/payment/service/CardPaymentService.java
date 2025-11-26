package uk.gov.netz.api.workflow.request.flow.payment.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.config.WebAppProperties;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.payment.client.service.GovukPayService;
import uk.gov.netz.api.workflow.payment.config.property.GovukPayProperties;
import uk.gov.netz.api.workflow.payment.domain.dto.PaymentCreateInfo;
import uk.gov.netz.api.workflow.payment.domain.dto.PaymentCreateResult;
import uk.gov.netz.api.workflow.payment.domain.dto.PaymentGetInfo;
import uk.gov.netz.api.workflow.payment.domain.dto.PaymentGetResult;
import uk.gov.netz.api.workflow.payment.domain.dto.PaymentStateInfo;
import uk.gov.netz.api.workflow.payment.domain.enumeration.PaymentMethodType;
import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.actionhandler.ProcessRequestTaskAspect;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.payment.RequestTypePaymentDescriptionResolver;
import uk.gov.netz.api.workflow.request.flow.payment.domain.CardPaymentCreateResponseDTO;
import uk.gov.netz.api.workflow.request.flow.payment.domain.CardPaymentProcessResponseDTO;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentMakeRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentOutcome;
import uk.gov.netz.api.workflow.request.flow.payment.transform.CardPaymentMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "govuk-pay", name = "isActive", havingValue = "true")
public class CardPaymentService {

    private final RequestTaskService requestTaskService;
    private final PaymentCompleteService paymentCompleteService;
    private final GovukPayService govukPayService;
    private final WorkflowService workflowService;
    private final WebAppProperties webAppProperties;
    private final GovukPayProperties govukPayProperties;

    private static final CardPaymentMapper CARD_PAYMENT_MAPPER = Mappers.getMapper(CardPaymentMapper.class);

    private final RequestTypePaymentDescriptionResolver requestTypePaymentDescriptionResolver;

    /**
     * Do not remove unused input parameters, since they are needed so as
     * validations introduced in {@link ProcessRequestTaskAspect} to be executed as well.
     */
    @Transactional
    //Seemingly unused parameters are actually needed in ProcessRequestTaskAspect, so they should not be removed
    public CardPaymentCreateResponseDTO createCardPayment(Long requestTaskId, String requestTaskActionType, AppUser authUser) {
        RequestTask requestTask = requestTaskService.findTaskById(requestTaskId);

        PaymentMakeRequestTaskPayload requestTaskPayload = (PaymentMakeRequestTaskPayload) requestTask.getPayload();
        validatePaymentMethod(requestTaskPayload);

        if (StringUtils.isEmpty(requestTaskPayload.getExternalPaymentId())) {
            PaymentCreateResult paymentCreateResult = govukPayService
                    .createPayment(buildPaymentCreateInfo(requestTask));

            requestTaskPayload.setExternalPaymentId(paymentCreateResult.getPaymentId());
            return CardPaymentCreateResponseDTO.builder().nextUrl(paymentCreateResult.getNextUrl()).build();

        } else {
            return CardPaymentCreateResponseDTO.builder().pendingPaymentExist(true).build();
        }
    }

    /**
     * Do not remove unused input parameters, since they are needed so as
     * validations introduced in {@link ProcessRequestTaskAspect} to be executed as well.
     */
    @Transactional
    //Seemingly unused parameters are actually needed in ProcessRequestTaskAspect, so they should not be removed
    public CardPaymentProcessResponseDTO processExistingCardPayment(Long requestTaskId, String requestTaskActionType, AppUser authUser) {
        RequestTask requestTask = requestTaskService.findTaskById(requestTaskId);

        PaymentGetResult paymentGetResult = govukPayService.getPayment(buildPaymentGetInfo(requestTask));
        PaymentStateInfo paymentStateInfo = paymentGetResult.getState();

        if (paymentStateInfo != null) {
            if (paymentStateInfo.isFinished()) {
                if ("success".equals(paymentStateInfo.getStatus())) {
                    completePaymentTask(requestTask, authUser);
                } else {
                    removeExternalPaymentIdFromTaskPayload(requestTask);
                }
            }
        } else {
            removeExternalPaymentIdFromTaskPayload(requestTask);
        }

        return CARD_PAYMENT_MAPPER.toCardPaymentProcessResponseDTO(paymentGetResult);
    }

    private PaymentCreateInfo buildPaymentCreateInfo(RequestTask requestTask) {
        Request request = requestTask.getRequest();
        PaymentMakeRequestTaskPayload requestTaskPayload = (PaymentMakeRequestTaskPayload) requestTask.getPayload();
        return PaymentCreateInfo.builder()
            .amount(requestTaskPayload.getAmount())
            .paymentRefNum(requestTaskPayload.getPaymentRefNum())
            .description(requestTypePaymentDescriptionResolver.resolveDescription(request.getType().getCode()))
            .returnUrl(constructReturnUrl(requestTask.getId()))
            .competentAuthority(request.getCompetentAuthority())
            .build();
    }

    private PaymentGetInfo buildPaymentGetInfo(RequestTask requestTask) {
        Request request = requestTask.getRequest();
        PaymentMakeRequestTaskPayload requestTaskPayload = (PaymentMakeRequestTaskPayload) requestTask.getPayload();
        String externalPaymentId = requestTaskPayload.getExternalPaymentId();

        if (StringUtils.isEmpty(externalPaymentId)) {
            throw new BusinessException(ErrorCode.EXTERNAL_PAYMENT_ID_NOT_EXIST, requestTask.getId());
        }

        return PaymentGetInfo.builder()
            .paymentId(externalPaymentId)
            .competentAuthority(request.getCompetentAuthority())
            .build();
    }

    private String constructReturnUrl(Long requestTaskId) {
        return UriComponentsBuilder
            .fromUriString(webAppProperties.getUrl())
            .path("/")
            .path(govukPayProperties.getConfirmationReturnUrl())
            .buildAndExpand(String.valueOf(requestTaskId), PaymentMethodType.CREDIT_OR_DEBIT_CARD.name())
            .toUriString();
    }

    private void validatePaymentMethod(PaymentMakeRequestTaskPayload requestTaskPayload) {
        if (!requestTaskPayload.getPaymentMethodTypes().contains(PaymentMethodType.CREDIT_OR_DEBIT_CARD)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
    }

    private void completePaymentTask(RequestTask requestTask, AppUser authUser) {
        paymentCompleteService.complete(requestTask.getRequest(), authUser);
        workflowService.completeTask(requestTask.getProcessTaskId(),
            Map.of(BpmnProcessConstants.PAYMENT_OUTCOME, PaymentOutcome.SUCCEEDED));
    }

    private void removeExternalPaymentIdFromTaskPayload(RequestTask requestTask) {
        PaymentMakeRequestTaskPayload requestTaskPayload = (PaymentMakeRequestTaskPayload) requestTask.getPayload();
        requestTaskPayload.setExternalPaymentId(null);
    }
}
