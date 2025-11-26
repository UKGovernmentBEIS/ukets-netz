package uk.gov.netz.api.workflow.request.flow.payment;

public class RequestTypeDefaultPaymentDescriptionResolver implements RequestTypePaymentDescriptionResolver {

    private final static String NOT_APPLICABLE = "N/A";

    @Override
    public String resolveDescription(String requestTypeCode) {
        return NOT_APPLICABLE;
    }
}
