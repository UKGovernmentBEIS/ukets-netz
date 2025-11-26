package uk.gov.netz.api.workflow.request.flow.common.domain.dto;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;

@Getter
@EqualsAndHashCode
public class RequestCreateAccountStatusValidationResult {

    private boolean valid;
    private String reportedAccountStatus;

    public RequestCreateAccountStatusValidationResult(boolean valid, AccountStatus reportedAccountStatus) {
        this.valid = valid;
        this.reportedAccountStatus = reportedAccountStatus != null ? reportedAccountStatus.getName() : null;
    }

    public RequestCreateAccountStatusValidationResult(boolean valid) {
        this.valid = valid;
    }
}
