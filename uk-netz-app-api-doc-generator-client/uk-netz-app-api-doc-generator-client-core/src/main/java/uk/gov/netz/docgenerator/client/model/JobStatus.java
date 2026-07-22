package uk.gov.netz.docgenerator.client.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatus {

    private String jobId;
    private JobState state;
    private String outputObjectKey;
    private ErrorDetail error;
    private Instant submittedAt;
}
