package uk.gov.netz.api.workflow.request.flow.common.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateRequestTypeValidationResult {

    private boolean valid;

    @Builder.Default
    private Set<String> reportedRequestTypes = new HashSet<>();
}
