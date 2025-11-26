package uk.gov.netz.api.workflow.request.core.domain.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestTaskActionPayloadTypes {

	public String RFI_SUBMIT_PAYLOAD = "RFI_SUBMIT_PAYLOAD";
	public String RFI_RESPONSE_SUBMIT_PAYLOAD = "RFI_RESPONSE_SUBMIT_PAYLOAD";
    
	public String RDE_SUBMIT_PAYLOAD = "RDE_SUBMIT_PAYLOAD";
	public String RDE_RESPONSE_SUBMIT_PAYLOAD = "RDE_RESPONSE_SUBMIT_PAYLOAD";
	public String RDE_FORCE_DECISION_PAYLOAD = "RDE_FORCE_DECISION_PAYLOAD";
    
	public String PAYMENT_MARK_AS_RECEIVED_PAYLOAD = "PAYMENT_MARK_AS_RECEIVED_PAYLOAD";
	public String PAYMENT_CANCEL_PAYLOAD = "PAYMENT_CANCEL_PAYLOAD";

	public String EMPTY_PAYLOAD = "EMPTY_PAYLOAD";
}
