package uk.gov.netz.api.workflow.request.flow.common.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateValidationResult {

    private boolean valid;

    // used for flows that can be allowed conditionally, in contrast to RequestCreateActionType.includedToAvailableWorkflows
    @JsonIgnore
    @Builder.Default
    private boolean isAvailable = true;

    @JsonProperty("accountStatus")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reportedAccountStatus;

    @JsonProperty("applicableAccountStatuses")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private Set<String> applicableAccountStatuses = new HashSet<>();

    @JsonProperty("requests")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private Set<String> reportedRequestTypes = new HashSet<>();

}
