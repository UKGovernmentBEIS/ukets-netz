package uk.gov.netz.api.workflow.request.core.assignment.requestassign.release;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestPayload;

@Service
@RequiredArgsConstructor
public class UserRoleTypeOperatorRequestReleaseService implements UserRoleTypeRequestReleaseService {

	@Override
	public String getRoleType() {
		return RoleTypeConstants.OPERATOR;
	}

	@Override
	public void release(Request request, String userId) {
		final RequestPayload requestPayload = request.getPayload();
        final String assignee = requestPayload.getOperatorAssignee();
		if (!StringUtils.isEmpty(assignee)
				&& (assignee.equals(userId) || StringUtils.isEmpty(userId))) {
			requestPayload.setOperatorAssignee(null);
		}
	}

}
