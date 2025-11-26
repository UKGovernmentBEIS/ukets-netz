package uk.gov.netz.api.workflow.request.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestActionInfoDTO {

    private Long id;
    private String type;
    private String submitter;
    private LocalDateTime creationDate;
}
