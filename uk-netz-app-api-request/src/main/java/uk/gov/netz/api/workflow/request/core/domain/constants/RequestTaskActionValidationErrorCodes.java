package uk.gov.netz.api.workflow.request.core.domain.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestTaskActionValidationErrorCodes {

    public String RFI_RDE_ALREADY_EXISTS = "RFI_RDE_ALREADY_EXISTS";
    public String PAYMENT_IN_PROGRESS = "PAYMENT_IN_PROGRESS";
    public String NO_VB_FOUND = "NO_VB_FOUND";
    public String NO_VERIFICATION_PERFORMED = "NO_VERIFICATION_PERFORMED";
    public String VERIFIED_DATA_FOUND = "VERIFIED_DATA_FOUND";
    public String VERIFICATION_NOT_ELIGIBLE = "VERIFICATION_NOT_ELIGIBLE";

}
