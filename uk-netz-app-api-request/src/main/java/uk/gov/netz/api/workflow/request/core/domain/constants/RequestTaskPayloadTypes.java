package uk.gov.netz.api.workflow.request.core.domain.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestTaskPayloadTypes {
	public String RFI_RESPONSE_SUBMIT_PAYLOAD = "RFI_RESPONSE_SUBMIT_PAYLOAD";

	public String RDE_RESPONSE_SUBMIT_PAYLOAD = "RDE_RESPONSE_SUBMIT_PAYLOAD";
	public String RDE_WAIT_FOR_RESPONSE_PAYLOAD = "RDE_WAIT_FOR_RESPONSE_PAYLOAD";

	public String PAYMENT_MAKE_PAYLOAD = "PAYMENT_MAKE_PAYLOAD";
	public String PAYMENT_TRACK_PAYLOAD = "PAYMENT_TRACK_PAYLOAD";
	public String PAYMENT_CONFIRM_PAYLOAD = "PAYMENT_CONFIRM_PAYLOAD";
	public String SYSTEM_MESSAGE_NOTIFICATION_PAYLOAD = "SYSTEM_MESSAGE_NOTIFICATION_PAYLOAD";
}
