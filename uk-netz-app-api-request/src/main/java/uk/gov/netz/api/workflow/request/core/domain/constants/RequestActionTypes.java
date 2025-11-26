package uk.gov.netz.api.workflow.request.core.domain.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestActionTypes {

	public String VERIFICATION_STATEMENT_CANCELLED = "VERIFICATION_STATEMENT_CANCELLED";
	public String PAYMENT_MARKED_AS_PAID = "PAYMENT_MARKED_AS_PAID";
	public String PAYMENT_MARKED_AS_RECEIVED = "PAYMENT_MARKED_AS_RECEIVED";
	public String PAYMENT_CANCELLED = "PAYMENT_CANCELLED";
	public String PAYMENT_COMPLETED = "PAYMENT_COMPLETED";
	public String RDE_SUBMITTED = "RDE_SUBMITTED";
	public String RDE_CANCELLED = "RDE_CANCELLED";
	public String RDE_FORCE_ACCEPTED = "RDE_FORCE_ACCEPTED";
	public String RDE_FORCE_REJECTED = "RDE_FORCE_REJECTED";
	public String RDE_EXPIRED = "RDE_EXPIRED";
	public String RDE_ACCEPTED = "RDE_ACCEPTED";
	public String RDE_REJECTED = "RDE_REJECTED";
	public String RFI_SUBMITTED = "RFI_SUBMITTED";
	public String RFI_CANCELLED = "RFI_CANCELLED";
	public String RFI_EXPIRED = "RFI_EXPIRED";
	public String RFI_RESPONSE_SUBMITTED = "RFI_RESPONSE_SUBMITTED";
	
}
