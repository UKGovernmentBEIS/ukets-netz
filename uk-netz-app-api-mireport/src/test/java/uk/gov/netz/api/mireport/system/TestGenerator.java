package uk.gov.netz.api.mireport.system;

import java.util.List;

import jakarta.persistence.EntityManager;
import uk.gov.netz.api.mireport.system.accountuserscontacts.AccountsUsersContactsMiReportResult;
import uk.gov.netz.api.mireport.system.executedactions.ExecutedRequestActionsMiReportParams;

public class TestGenerator implements MiReportSystemGenerator<ExecutedRequestActionsMiReportParams> {

	@Override
	public MiReportSystemResult generateMiReport(EntityManager entityManager,
			ExecutedRequestActionsMiReportParams reportParams) {
		return AccountsUsersContactsMiReportResult.builder().reportType(getReportType())
				.columnNames(List.of("col1", "col2")).build();
	}

	@Override
	public String getReportType() {
		return "reportType";
	}

}
