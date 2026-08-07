package uk.gov.netz.api.mireport.userdefined.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MiReportUserDefinedHistoryDTO {

    private LocalDateTime submissionDate;
    private String submittedBy;
    private String reasonForChange;
    private String reportName;
    private String categories;
    private String description;
    private String queryDefinition;
    private MiReportUserDefinedChangeType changeType;

}
