package uk.gov.netz.api.workflow.request.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestTaskActionValidationResult {

    private boolean valid;

    private String errorCode;

    public static RequestTaskActionValidationResult validResult() {
        return RequestTaskActionValidationResult.builder().valid(true).build();
    }

    public static RequestTaskActionValidationResult invalidResult(String errorCode) {
        return RequestTaskActionValidationResult.builder().valid(false).errorCode(errorCode).build();
    }
}
