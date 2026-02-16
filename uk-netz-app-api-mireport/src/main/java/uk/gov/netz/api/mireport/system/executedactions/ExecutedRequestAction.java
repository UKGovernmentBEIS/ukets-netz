package uk.gov.netz.api.mireport.system.executedactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutedRequestAction {

    @JsonProperty(value = "Account ID")
    private String accountId;

    @JsonProperty(value = "Account name")
    private String accountName;

    @JsonProperty(value = "Account status")
    private String accountStatus;

    @JsonProperty(value = "Workflow ID")
    private String requestId;

    @JsonProperty(value = "Workflow type")
    private String requestType;

    @JsonProperty(value = "Workflow status")
    private String requestStatus;

    @JsonProperty(value = "Timeline event type")
    private String requestActionType;

    @JsonProperty(value = "Timeline event Completed by")
    private String requestActionSubmitter;

    @JsonProperty(value = "Timeline event Date Completed")
    private LocalDateTime requestActionCompletionDate;

    public static List<String> getColumnNames() {
        return List.of("Account ID", "Account name", "Account status",
                "Workflow ID", "Workflow type", "Workflow status", "Timeline event type",
                "Timeline event Completed by", "Timeline event Date Completed");
    }
}
