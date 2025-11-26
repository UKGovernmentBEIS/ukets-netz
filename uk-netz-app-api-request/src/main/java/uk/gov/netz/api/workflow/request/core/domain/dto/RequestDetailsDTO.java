package uk.gov.netz.api.workflow.request.core.domain.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.netz.api.workflow.request.core.domain.RequestMetadata;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
public class RequestDetailsDTO {

    private String id;
    private String requestType;
    private String requestStatus;
    private LocalDate creationDate;
    private RequestMetadata requestMetadata;

    public RequestDetailsDTO(String id, String requestType, String requestStatus, LocalDateTime creationDate,
                             RequestMetadata requestMetadata) {
        this.id = id;
        this.requestType = requestType;
        this.requestStatus = requestStatus;
        this.creationDate = creationDate.toLocalDate();
        this.requestMetadata = requestMetadata;
    }
}
