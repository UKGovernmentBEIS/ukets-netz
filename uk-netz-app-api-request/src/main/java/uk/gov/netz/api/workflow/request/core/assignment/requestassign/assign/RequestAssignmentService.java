package uk.gov.netz.api.workflow.request.core.assignment.requestassign.assign;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.Request;

@Log4j2
@Service
@RequiredArgsConstructor
public class RequestAssignmentService {

    private final UserRoleTypeService userRoleTypeService;
    private final List<UserRoleTypeRequestAssignmentService> userRoleTypeRequestAssignmentServices;
    
    /**
     * Set the provided user as assignee to the provided {@link Request}
     * @param request the {@link Request}
     * @param userId the user id
     */
    @Transactional
    public void assignRequestToUser(Request request, String userId) {
        if (ObjectUtils.isEmpty(userId)) {
            log.error("A non empty user should be assigned request '{}'",request::getId);
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_ALLOWED);
        }

        final String userRoleType = userRoleTypeService.getUserRoleTypeByUserId(userId).getRoleType();
        
        userRoleTypeRequestAssignmentServices
			.stream()
			.filter(service -> service.getRoleType().equals(userRoleType)).findAny()
				.orElseThrow(() -> new UnsupportedOperationException(
							String.format("User with role type %s not related with request assignment", userRoleType)))
			.assign(request, userId);
    }

}
