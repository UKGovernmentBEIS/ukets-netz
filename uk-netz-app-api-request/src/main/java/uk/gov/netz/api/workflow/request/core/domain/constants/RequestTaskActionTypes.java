package uk.gov.netz.api.workflow.request.core.domain.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestTaskActionTypes {

	public String CANCEL_APPLICATION = "CANCEL_APPLICATION";
	public String RFI_UPLOAD_ATTACHMENT = "RFI_UPLOAD_ATTACHMENT";
	public String RFI_SUBMIT = "RFI_SUBMIT";
	public String RDE_SUBMIT = "RDE_SUBMIT";
	public String RDE_FORCE_DECISION = "RDE_FORCE_DECISION";
	public String RDE_RESPONSE_SUBMIT = "RDE_RESPONSE_SUBMIT";
	public String RDE_UPLOAD_ATTACHMENT = "RDE_UPLOAD_ATTACHMENT";
	public String PAYMENT_MARK_AS_PAID = "PAYMENT_MARK_AS_PAID";
	public String PAYMENT_MARK_AS_RECEIVED = "PAYMENT_MARK_AS_RECEIVED";
	public String PAYMENT_PAY_BY_CARD = "PAYMENT_PAY_BY_CARD";
	public String SYSTEM_MESSAGE_DISMISS = "SYSTEM_MESSAGE_DISMISS";
}
