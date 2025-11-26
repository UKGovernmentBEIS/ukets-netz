package uk.gov.netz.api.workflow.request.core.assignment.requestassign.assign;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestPayload;

@Service
@RequiredArgsConstructor
public class UserRoleTypeVerifierRequestAssignmentService implements UserRoleTypeRequestAssignmentService {

	@Override
	public String getRoleType() {
		return RoleTypeConstants.VERIFIER;
	}

	@Override
	public void assign(Request request, String userId) {
		final RequestPayload requestPayload = request.getPayload();
		if (!userId.equals(requestPayload.getVerifierAssignee())) {
			requestPayload.setVerifierAssignee(userId);
		}
	}

}
