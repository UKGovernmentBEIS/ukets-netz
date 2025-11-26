package uk.gov.netz.api.workflow.request.flow.common.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.flow.common.domain.DecisionNotification;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class DecisionNotificationUsersValidator {

    private final WorkflowUsersValidator workflowUsersValidator;

    public boolean areUsersValid(final RequestTask requestTask, final DecisionNotification decisionNotification,
            final AppUser appUser) {

        final Long accountId = requestTask.getRequest().getAccountId();
        final Set<String> operators = decisionNotification.getOperators();
        final boolean operatorsValid = workflowUsersValidator.areOperatorsValid(accountId, operators, appUser);
        if (!operatorsValid) {
            return false;
        }

        final Set<Long> externalContacts = decisionNotification.getExternalContacts();
        final boolean externalContactValid = workflowUsersValidator.areExternalContactsValid(externalContacts,
                appUser);
        if (!externalContactValid) {
            return false;
        }

        final String signatory = decisionNotification.getSignatory();
        return workflowUsersValidator.isSignatoryValid(requestTask, signatory);
    }
}
