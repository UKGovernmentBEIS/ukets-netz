package uk.gov.netz.docgenerator.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {

    private String jobId;
    private String errorReason;
    private String timestamp;
    private int attemptCount;
}
