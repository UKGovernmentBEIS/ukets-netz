package uk.gov.netz.api.workflow.request.flow.common.jsonprovider;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.common.config.jackson.JsonSubTypesProvider;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskPayloadTypes;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentConfirmRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentMakeRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentTrackRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeForceDecisionRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeResponseRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiResponseSubmitRequestTaskPayload;

import java.util.List;

@Component
public class RequestTaskPayloadCommonTypesProvider implements JsonSubTypesProvider {

	@Override
	public List<NamedType> getTypes() {
		return List.of(
				new NamedType(RfiResponseSubmitRequestTaskPayload.class, RequestTaskPayloadTypes.RFI_RESPONSE_SUBMIT_PAYLOAD),
				
				new NamedType(RdeForceDecisionRequestTaskPayload.class, RequestTaskPayloadTypes.RDE_WAIT_FOR_RESPONSE_PAYLOAD),
				new NamedType(RdeResponseRequestTaskPayload.class, RequestTaskPayloadTypes.RDE_RESPONSE_SUBMIT_PAYLOAD),
				
				new NamedType(PaymentMakeRequestTaskPayload.class, RequestTaskPayloadTypes.PAYMENT_MAKE_PAYLOAD),
				new NamedType(PaymentTrackRequestTaskPayload.class, RequestTaskPayloadTypes.PAYMENT_TRACK_PAYLOAD),
				new NamedType(PaymentConfirmRequestTaskPayload.class, RequestTaskPayloadTypes.PAYMENT_CONFIRM_PAYLOAD),
				new NamedType(SystemMessageNotificationRequestTaskPayload.class, RequestTaskPayloadTypes.SYSTEM_MESSAGE_NOTIFICATION_PAYLOAD)
				);
	}

}
