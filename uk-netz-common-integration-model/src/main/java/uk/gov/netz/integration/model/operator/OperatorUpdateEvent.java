package uk.gov.netz.integration.model.operator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorUpdateEvent {

    private Long operatorId;
    private String emitterId;
    private String regulator;
}
