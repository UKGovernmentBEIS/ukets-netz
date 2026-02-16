package uk.gov.netz.api.mireport.system.outstandingrequesttasks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OutstandingRequestTask {

    @JsonProperty(value = "Account ID")
    private String accountId;

    @JsonProperty(value = "Account name")
    private String accountName;

    @JsonProperty(value = "Workflow ID")
    private String requestId;

    @JsonProperty(value = "Workflow type")
    private String requestType;

    @JsonProperty(value = "Workflow task name")
    private String requestTaskType;

    private String requestTaskAssignee;

    @JsonProperty(value = "Workflow task assignee")
    private String requestTaskAssigneeName;

    @JsonProperty(value = "Workflow task due date")
    private LocalDate requestTaskDueDate;

    @JsonIgnore
    private LocalDate requestTaskPausedDate;

    @JsonProperty(value = "Workflow task days remaining")
    private Long requestTaskRemainingDays;
    
    public OutstandingRequestTask(final String accountId,
                                  final String accountName,
                                  final String requestId,
                                  final String requestType,
                                  final String requestTaskType,
                                  final String requestTaskAssignee,
                                  final LocalDate requestTaskDueDate, 
                                  final LocalDate requestTaskPausedDate) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.requestId = requestId;
        this.requestType = requestType;
        this.requestTaskType = requestTaskType;
        this.requestTaskAssignee = requestTaskAssignee;
        this.requestTaskDueDate = requestTaskDueDate;
        if (requestTaskDueDate != null) {
            this.requestTaskRemainingDays = ChronoUnit.DAYS.between(requestTaskPausedDate == null ? LocalDate.now() : requestTaskPausedDate, requestTaskDueDate);
        }
    }

    public static List<String> getColumnNames() {
        return List.of("Account ID", "Account name",
                "Workflow ID", "Workflow type", "Workflow task name",
                "Workflow task assignee", "Workflow task due date", "Workflow task days remaining");
    }
}
