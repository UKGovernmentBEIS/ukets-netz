package uk.gov.netz.api.workflow.request.core.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "payloadType", visible = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class RequestPayload implements Payload {

    private String payloadType;

    private String operatorAssignee;

    private String regulatorAssignee;
    
    private String verifierAssignee;

    private String regulatorPeerReviewer;

    private String regulatorReviewer;
    
    private Boolean paymentCompleted;
    
    private BigDecimal paymentAmount;
}
