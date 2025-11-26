package uk.gov.netz.api.workflow.request.flow.common.jsonprovider;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.jsontype.NamedType;

import uk.gov.netz.api.common.config.jackson.JsonSubTypesProvider;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionPayloadTypes;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentCancelledRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentProcessedRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeDecisionForcedRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeRejectedRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeSubmittedRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiResponseSubmittedRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiSubmittedRequestActionPayload;

@Component
public class RequestActionPayloadCommonTypesProvider implements JsonSubTypesProvider {

	@Override
	public List<NamedType> getTypes() {
		return List.of(
				new NamedType(RfiResponseSubmittedRequestActionPayload.class, RequestActionPayloadTypes.RFI_RESPONSE_SUBMITTED_PAYLOAD),
				new NamedType(RfiSubmittedRequestActionPayload.class, RequestActionPayloadTypes.RFI_SUBMITTED_PAYLOAD),
				
				new NamedType(RdeDecisionForcedRequestActionPayload.class, RequestActionPayloadTypes.RDE_DECISION_FORCED_PAYLOAD),
				new NamedType(RdeRejectedRequestActionPayload.class, RequestActionPayloadTypes.RDE_REJECTED_PAYLOAD),
				new NamedType(RdeSubmittedRequestActionPayload.class, RequestActionPayloadTypes.RDE_SUBMITTED_PAYLOAD),
				
				new NamedType(PaymentProcessedRequestActionPayload.class, RequestActionPayloadTypes.PAYMENT_MARKED_AS_PAID_PAYLOAD),
				new NamedType(PaymentProcessedRequestActionPayload.class, RequestActionPayloadTypes.PAYMENT_MARKED_AS_RECEIVED_PAYLOAD),
				new NamedType(PaymentProcessedRequestActionPayload.class, RequestActionPayloadTypes.PAYMENT_COMPLETED_PAYLOAD),
				new NamedType(PaymentCancelledRequestActionPayload.class, RequestActionPayloadTypes.PAYMENT_CANCELLED_PAYLOAD)
				);
	}

}
