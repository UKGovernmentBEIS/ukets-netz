package uk.gov.netz.api.mireport.system.executedactions;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.common.validation.SpELExpression;
import uk.gov.netz.api.mireport.system.MiReportSystemParams;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SpELExpression(expression =
    "{(#fromDate == null) " +
    "|| (#toDate == null) " +
    "|| T(java.time.LocalDate).parse(#fromDate).isBefore(T(java.time.LocalDate).parse(#toDate)) " +
    "|| T(java.time.LocalDate).parse(#fromDate).isEqual(T(java.time.LocalDate).parse(#toDate))}",
    message = "mireport.executedRequestActions.fromDate.toDate")
public class ExecutedRequestActionsMiReportParams extends MiReportSystemParams {

    @NotNull(message = "{mireport.executedRequestActions.fromDate.notEmpty}")
    private LocalDate fromDate;

    private LocalDate toDate;
}
