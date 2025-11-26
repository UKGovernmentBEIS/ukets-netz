package uk.gov.netz.api.workflow.request.flow.common.constants;


import lombok.Getter;

import java.util.HashMap;

public class NotificationTemplateWorkflowTaskType {

    public static final String RFI = "RFI";
    public static final String RDE = "RDE";
    public static final String PAYMENT = "PAYMENT";

    @Getter
    private static HashMap<String, String> values;

    static {
        values = new HashMap<>();
        values.put(RFI, "Request for Information");
        values.put(RDE, "Determination extension request");
        values.put(PAYMENT, "Payment");
    }

    public static void add(String requestType, String description) {
        values.put(requestType, description);
    }

    public static String getDescription(String requestType) {
        return values.computeIfAbsent(requestType, s -> { throw new IllegalArgumentException(
            String.format("Request type %s cannot be mapped to notification template workflow task type",
                requestType));
        });
    }
}
