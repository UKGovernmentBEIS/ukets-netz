package uk.gov.netz.integration.model.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationEventErrorDetails {

    private IntegrationEventError error;
    private String errorMessage;
}
