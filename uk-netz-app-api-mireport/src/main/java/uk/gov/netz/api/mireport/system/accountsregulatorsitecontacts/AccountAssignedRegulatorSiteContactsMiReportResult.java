package uk.gov.netz.api.mireport.system.accountsregulatorsitecontacts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.mireport.system.MiReportSystemResult;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AccountAssignedRegulatorSiteContactsMiReportResult<T extends AccountAssignedRegulatorSiteContact> extends MiReportSystemResult {

    private List<T> results;
}
