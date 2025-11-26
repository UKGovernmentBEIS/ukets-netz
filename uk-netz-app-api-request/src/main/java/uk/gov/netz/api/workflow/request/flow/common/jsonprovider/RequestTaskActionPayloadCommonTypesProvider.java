package uk.gov.netz.api.workflow.request.flow.common.jsonprovider;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.jsontype.NamedType;

import uk.gov.netz.api.common.config.jackson.JsonSubTypesProvider;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionPayloadTypes;
import uk.gov.netz.api.workflow.request.flow.common.domain.RequestTaskActionEmptyPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentCancelRequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentMarkAsReceivedRequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeForceDecisionRequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeResponseSubmitRequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeSubmitRequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiResponseSubmitRequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiSubmitRequestTaskActionPayload;

@Component
public class RequestTaskActionPayloadCommonTypesProvider implements JsonSubTypesProvider {

	@Override
	public List<NamedType> getTypes() {
		return List.of(
				new NamedType(RfiSubmitRequestTaskActionPayload.class, RequestTaskActionPayloadTypes.RFI_SUBMIT_PAYLOAD),
				new NamedType(RfiResponseSubmitRequestTaskActionPayload.class, RequestTaskActionPayloadTypes.RFI_RESPONSE_SUBMIT_PAYLOAD),
				
				new NamedType(RdeSubmitRequestTaskActionPayload.class, RequestTaskActionPayloadTypes.RDE_SUBMIT_PAYLOAD),
				new NamedType(RdeForceDecisionRequestTaskActionPayload.class, RequestTaskActionPayloadTypes.RDE_FORCE_DECISION_PAYLOAD),
				new NamedType(RdeResponseSubmitRequestTaskActionPayload.class, RequestTaskActionPayloadTypes.RDE_RESPONSE_SUBMIT_PAYLOAD),
				
				new NamedType(PaymentMarkAsReceivedRequestTaskActionPayload.class, RequestTaskActionPayloadTypes.PAYMENT_MARK_AS_RECEIVED_PAYLOAD),
				new NamedType(PaymentCancelRequestTaskActionPayload.class, RequestTaskActionPayloadTypes.PAYMENT_CANCEL_PAYLOAD),
				
				new NamedType(RequestTaskActionEmptyPayload.class, RequestTaskActionPayloadTypes.EMPTY_PAYLOAD)
				);
	}

}
