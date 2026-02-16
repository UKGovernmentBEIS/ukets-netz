package uk.gov.netz.api.mireport.system.outstandingrequesttasks;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.mireport.system.MiReportSystemGenerator;
import uk.gov.netz.api.mireport.system.MiReportSystemResult;
import uk.gov.netz.api.mireport.system.MiReportSystemType;
import uk.gov.netz.api.userinfoapi.UserInfo;
import uk.gov.netz.api.userinfoapi.UserInfoApi;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class OutstandingRequestTasksReportGenerator<U extends OutstandingRequestTask> implements MiReportSystemGenerator<OutstandingRegulatorRequestTasksMiReportParams> {

    private final OutstandingRequestTasksReportService outstandingRequestTasksReportService;
    private final UserInfoApi userInfoApi;

    public String getReportType() {
        return MiReportSystemType.REGULATOR_OUTSTANDING_REQUEST_TASKS;
    }

    public abstract List<U> findOutstandingRequestTaskParams(EntityManager entityManager,
                                                                                                OutstandingRegulatorRequestTasksMiReportParams reportParams);

    public MiReportSystemResult generateMiReport(EntityManager entityManager, OutstandingRegulatorRequestTasksMiReportParams reportParams) {
        Set<String> regulatorRequestTaskTypes =
                outstandingRequestTasksReportService.getRequestTaskTypesByRoleType(RoleTypeConstants.REGULATOR);
        reportParams.getRequestTaskTypes().retainAll(regulatorRequestTaskTypes);

        if (reportParams.getRequestTaskTypes().isEmpty()) {
            reportParams.setRequestTaskTypes(regulatorRequestTaskTypes);
        }

        List<U> outstandingRequestTasks =
                findOutstandingRequestTaskParams(entityManager, reportParams);

        this.resolveAssigneeNames(outstandingRequestTasks);

        return OutstandingRequestTasksMiReportResult.<U>builder()
                .reportType(getReportType())
                .columnNames(getColumnNames())
                .results(outstandingRequestTasks)
                .build();
    }

    private void resolveAssigneeNames(final List<U> outstandingRequestTasks) {

        final List<String> assigneeIds = outstandingRequestTasks.stream()
                .map(OutstandingRequestTask::getRequestTaskAssignee)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        final List<UserInfo> assignees = userInfoApi.getUsers(assigneeIds);
        final Map<String, String> assigneeNames = assignees.stream()
                .collect(Collectors.toMap(
                                UserInfo::getId,
                                ui -> ui.getFirstName() + " " + ui.getLastName()
                        )
                );
        outstandingRequestTasks.stream()
                .filter(ort -> ort.getRequestTaskAssignee() != null)
                .forEach(
                        ort -> ort.setRequestTaskAssigneeName(assigneeNames.get(ort.getRequestTaskAssignee()))
                );
    }

    public abstract List<String> getColumnNames();

}
