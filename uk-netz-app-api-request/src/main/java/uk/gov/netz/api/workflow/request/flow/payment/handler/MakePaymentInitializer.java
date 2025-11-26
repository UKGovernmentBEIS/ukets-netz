package uk.gov.netz.api.workflow.request.flow.payment.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.payment.domain.enumeration.PaymentMethodType;
import uk.gov.netz.api.workflow.payment.service.BankAccountDetailsService;
import uk.gov.netz.api.workflow.payment.service.PaymentMethodService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;
import uk.gov.netz.api.workflow.request.core.service.InitializeRequestTaskHandler;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentMakeRequestTaskPayload;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MakePaymentInitializer implements InitializeRequestTaskHandler {

	private final RequestTaskTypeRepository requestTaskTypeRepository;
    private final PaymentMethodService paymentMethodService;
    private final BankAccountDetailsService bankAccountDetailsService;

    @Override
    public RequestTaskPayload initializePayload(Request request) {
        CompetentAuthorityEnum competentAuthority = request.getCompetentAuthority();
        Set<PaymentMethodType> paymentMethodTypes = paymentMethodService.getPaymentMethodTypesByCa(competentAuthority);

        PaymentMakeRequestTaskPayload paymentMakeRequestTaskPayload = PaymentMakeRequestTaskPayload.builder()
            .payloadType(RequestTaskPayloadTypes.PAYMENT_MAKE_PAYLOAD)
            .amount(request.getPayload().getPaymentAmount())
            .paymentRefNum(request.getId())
            .creationDate(LocalDate.now())
            .paymentMethodTypes(paymentMethodTypes)
            .build();
        
        if (paymentMethodTypes.contains(PaymentMethodType.BANK_TRANSFER)) {
            paymentMakeRequestTaskPayload.setBankAccountDetails(bankAccountDetailsService.getBankAccountDetailsByCa(competentAuthority));
        }

        return paymentMakeRequestTaskPayload;
    }

    @Override
    public Set<String> getRequestTaskTypes() {
    	return requestTaskTypeRepository.findAllByCodeEndingWith(RequestTaskTypes.MAKE_PAYMENT).stream()
				.map(RequestTaskType::getCode).collect(Collectors.toSet());
    }
}
