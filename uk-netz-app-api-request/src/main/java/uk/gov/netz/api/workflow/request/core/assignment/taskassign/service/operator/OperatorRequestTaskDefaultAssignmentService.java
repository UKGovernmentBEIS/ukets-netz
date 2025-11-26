package uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.operator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.netz.api.account.service.AccountContactQueryService;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessCheckedException;
import uk.gov.netz.api.workflow.request.core.assignment.requestassign.release.RequestReleaseService;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.RequestTaskAssignmentService;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.UserRoleRequestTaskDefaultAssignmentService;
import uk.gov.netz.api.workflow.request.core.domain.RequestPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OperatorRequestTaskDefaultAssignmentService implements UserRoleRequestTaskDefaultAssignmentService {

    private final RequestTaskAssignmentService requestTaskAssignmentService;
    private final AccountContactQueryService accountContactQueryService;
    private final UserRoleTypeService userRoleTypeService;
    private final RequestReleaseService requestReleaseService;

    @Override
    public String getRoleType() {
        return RoleTypeConstants.OPERATOR;
    }

    @Transactional
    public void assignDefaultAssigneeToTask(RequestTask requestTask) {
        RequestPayload requestPayload = requestTask.getRequest().getPayload();
        String candidateAssignee = requestPayload.getOperatorAssignee();

        if (!ObjectUtils.isEmpty(candidateAssignee) && userRoleTypeService.isUserOperator(candidateAssignee)) {
            try {
                requestTaskAssignmentService.assignToUser(requestTask, candidateAssignee);
            } catch (BusinessCheckedException e) {
                assignTaskToAccountPrimaryContactOrReleaseRequest(requestTask);
            }
        } else {
            assignTaskToAccountPrimaryContactOrReleaseRequest(requestTask);
        }
    }

    private void assignTaskToAccountPrimaryContactOrReleaseRequest(RequestTask requestTask) {
        Optional<String> accountPrimaryContactOptional =
            accountContactQueryService.findPrimaryContactByAccount(requestTask.getRequest().getAccountId());

        accountPrimaryContactOptional.ifPresentOrElse(
            primaryContact -> {
                try {
                    requestTaskAssignmentService.assignToUser(requestTask, primaryContact);
                } catch (BusinessCheckedException e) {
                    requestReleaseService.releaseRequest(requestTask);
                }
            },
            () -> requestReleaseService.releaseRequest(requestTask)
        );
    }
}
