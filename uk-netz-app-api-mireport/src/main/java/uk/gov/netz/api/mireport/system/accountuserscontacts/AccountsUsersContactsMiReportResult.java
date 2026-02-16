package uk.gov.netz.api.mireport.system.accountuserscontacts;

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
public class AccountsUsersContactsMiReportResult<T extends AccountUserContact> extends MiReportSystemResult {

    private List<T> results;
}
