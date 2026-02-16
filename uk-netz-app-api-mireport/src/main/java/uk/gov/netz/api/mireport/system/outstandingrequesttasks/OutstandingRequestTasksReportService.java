package uk.gov.netz.api.mireport.system.outstandingrequesttasks;

import java.util.Set;

public interface OutstandingRequestTasksReportService {
    Set<String> getRequestTaskTypesByRoleType(String roleType);
}
