package uk.gov.netz.api.workflow.request.flow.payment.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionPayload;
import uk.gov.netz.api.common.validation.NotAfterCurrentDateInZone;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentMarkAsReceivedRequestTaskActionPayload extends RequestTaskActionPayload {

    @NotNull
    @NotAfterCurrentDateInZone
    private LocalDate receivedDate;
}
