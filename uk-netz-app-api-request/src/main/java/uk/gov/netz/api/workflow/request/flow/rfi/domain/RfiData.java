package uk.gov.netz.api.workflow.request.flow.rfi.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfiData {

    private RfiQuestionPayload rfiQuestionPayload;
    private LocalDate rfiDeadline;
    private RfiResponsePayload rfiResponsePayload;

    @Builder.Default
    private Map<UUID, String> rfiAttachments = new HashMap<>();

}
