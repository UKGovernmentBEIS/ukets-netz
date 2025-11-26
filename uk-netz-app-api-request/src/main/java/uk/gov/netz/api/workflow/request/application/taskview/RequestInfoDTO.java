package uk.gov.netz.api.workflow.request.application.taskview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.core.domain.RequestMetadata;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestInfoDTO {

    private String id;
    private String type;
    private CompetentAuthorityEnum competentAuthority;
    private Long accountId;
    private RequestMetadata requestMetadata;
    private Boolean paymentCompleted;
    private BigDecimal paymentAmount;
    private LocalDateTime creationDate;

    public RequestInfoDTO(final String id, final String type) {
        this.id = id;
        this.type = type;
    }
}
