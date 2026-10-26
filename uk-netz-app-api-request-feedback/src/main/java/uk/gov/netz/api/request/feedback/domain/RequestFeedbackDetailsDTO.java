package uk.gov.netz.api.request.feedback.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestFeedbackDetailsDTO {

    private Long id;
    private String requestId;
    private RequestFeedbackStatus status;
    private String type;
    private boolean expired;
}
