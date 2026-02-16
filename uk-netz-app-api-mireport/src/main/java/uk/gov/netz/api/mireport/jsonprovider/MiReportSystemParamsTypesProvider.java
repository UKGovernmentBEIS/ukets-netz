package uk.gov.netz.api.mireport.jsonprovider;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.common.config.jackson.JsonSubTypesProvider;
import uk.gov.netz.api.mireport.system.executedactions.ExecutedRequestActionsMiReportParams;
import uk.gov.netz.api.mireport.system.outstandingrequesttasks.OutstandingRegulatorRequestTasksMiReportParams;
import uk.gov.netz.api.mireport.system.EmptyMiReportSystemParams;
import uk.gov.netz.api.mireport.system.MiReportSystemType;

import java.util.List;

@Component
public class MiReportSystemParamsTypesProvider implements JsonSubTypesProvider {

	@Override
	public List<NamedType> getTypes() {
		return List.of(
				new NamedType(EmptyMiReportSystemParams.class, MiReportSystemType.LIST_OF_ACCOUNTS_USERS_CONTACTS),
				new NamedType(ExecutedRequestActionsMiReportParams.class, MiReportSystemType.COMPLETED_WORK),
				new NamedType(OutstandingRegulatorRequestTasksMiReportParams.class, MiReportSystemType.REGULATOR_OUTSTANDING_REQUEST_TASKS),
				new NamedType(EmptyMiReportSystemParams.class, MiReportSystemType.LIST_OF_ACCOUNTS_ASSIGNED_REGULATOR_SITE_CONTACTS)
				);
	}

}
