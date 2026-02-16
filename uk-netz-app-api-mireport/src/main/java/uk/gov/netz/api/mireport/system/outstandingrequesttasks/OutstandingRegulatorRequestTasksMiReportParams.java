package uk.gov.netz.api.mireport.system.outstandingrequesttasks;

import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.mireport.system.MiReportSystemParams;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OutstandingRegulatorRequestTasksMiReportParams extends MiReportSystemParams {

    @Default
    private Set<String> requestTaskTypes = new HashSet<>();

    @Default
    private Set<String> userIds = new HashSet<>();
}
