package uk.gov.netz.api.mireport.system.executedactions;

import jakarta.persistence.EntityManager;
import uk.gov.netz.api.mireport.system.MiReportSystemGenerator;
import uk.gov.netz.api.mireport.system.MiReportSystemResult;
import uk.gov.netz.api.mireport.system.MiReportSystemType;

import java.util.List;

public abstract class ExecutedRequestActionsReportGenerator<U extends ExecutedRequestAction> implements MiReportSystemGenerator<ExecutedRequestActionsMiReportParams> {

    public abstract List<U> findExecutedRequestActions(EntityManager entityManager, ExecutedRequestActionsMiReportParams reportParams);

    public String getReportType() {
        return MiReportSystemType.COMPLETED_WORK;
    }

    public MiReportSystemResult generateMiReport(EntityManager entityManager, ExecutedRequestActionsMiReportParams reportParams) {
        return ExecutedRequestActionsMiReportResult.<U>builder()
                .reportType(getReportType())
                .columnNames(getColumnNames())
                .results(findExecutedRequestActions(entityManager, reportParams))
                .build();
    }

    public abstract List<String> getColumnNames();

}
