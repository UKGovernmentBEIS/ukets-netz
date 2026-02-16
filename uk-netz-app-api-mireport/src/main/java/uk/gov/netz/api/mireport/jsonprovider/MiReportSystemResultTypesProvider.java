package uk.gov.netz.api.mireport.jsonprovider;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.common.config.jackson.JsonSubTypesProvider;
import uk.gov.netz.api.mireport.system.accountsregulatorsitecontacts.AccountAssignedRegulatorSiteContactsMiReportResult;
import uk.gov.netz.api.mireport.system.accountuserscontacts.AccountsUsersContactsMiReportResult;
import uk.gov.netz.api.mireport.system.executedactions.ExecutedRequestActionsMiReportResult;
import uk.gov.netz.api.mireport.system.outstandingrequesttasks.OutstandingRequestTasksMiReportResult;
import uk.gov.netz.api.mireport.system.MiReportSystemType;

import java.util.List;

@Component
public class MiReportSystemResultTypesProvider implements JsonSubTypesProvider {

	@Override
	public List<NamedType> getTypes() {
		return List.of(
				new NamedType(AccountsUsersContactsMiReportResult.class, MiReportSystemType.LIST_OF_ACCOUNTS_USERS_CONTACTS),
				new NamedType(ExecutedRequestActionsMiReportResult.class, MiReportSystemType.COMPLETED_WORK),
				new NamedType(OutstandingRequestTasksMiReportResult.class, MiReportSystemType.REGULATOR_OUTSTANDING_REQUEST_TASKS),
				new NamedType(AccountAssignedRegulatorSiteContactsMiReportResult.class, MiReportSystemType.LIST_OF_ACCOUNTS_ASSIGNED_REGULATOR_SITE_CONTACTS)
				);
	}

}
