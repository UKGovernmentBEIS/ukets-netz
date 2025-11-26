package uk.gov.netz.api.workflow.request.core.assignment.requestassign.release;

import uk.gov.netz.api.workflow.request.core.domain.Request;

public interface UserRoleTypeRequestReleaseService {

	String getRoleType();
	
	/**
	 * Release the provided request from the provided user id
	 * @param request
	 * @param userId
	 */
	void release(Request request, String userId);
	
}
