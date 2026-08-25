package uk.gov.netz.api.workflow.request.flow.rde.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.common.validation.NotBeforeCurrentDateInZone;

import java.time.LocalDate;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RdeResponsePayload {

    @NotNull
    @NotBeforeCurrentDateInZone(inclusive = false)
    private LocalDate currentDueDate;

    @NotNull
    @NotBeforeCurrentDateInZone(inclusive = false)
    private LocalDate proposedDueDate;
}
