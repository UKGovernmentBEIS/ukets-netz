package uk.gov.netz.api.workflow.request.core.assignment.requestassign.assign;

import uk.gov.netz.api.workflow.request.core.domain.Request;

public interface UserRoleTypeRequestAssignmentService {
	
	String getRoleType();
	
	/**
	 * Assign the provided request to the provided user id
	 * @param request
	 * @param userId
	 */
	void assign(Request request, String userId);

}
