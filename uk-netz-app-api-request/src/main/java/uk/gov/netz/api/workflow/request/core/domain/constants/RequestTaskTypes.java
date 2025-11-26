package uk.gov.netz.api.workflow.request.core.domain.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestTaskTypes {

	public String WAIT_FOR_RFI_RESPONSE = "WAIT_FOR_RFI_RESPONSE";
	public String RFI_RESPONSE_SUBMIT = "RFI_RESPONSE_SUBMIT";
	public String WAIT_FOR_RDE_RESPONSE = "WAIT_FOR_RDE_RESPONSE";
	public String RDE_RESPONSE_SUBMIT = "RDE_RESPONSE_SUBMIT";
	public String APPLICATION_VERIFICATION_SUBMIT = "APPLICATION_VERIFICATION_SUBMIT";
	public String MAKE_PAYMENT = "MAKE_PAYMENT";
	public String TRACK_PAYMENT = "TRACK_PAYMENT";
	public String CONFIRM_PAYMENT = "CONFIRM_PAYMENT";
	public String SYSTEM_NOTIFICATION = "SYSTEM_NOTIFICATION";
}
