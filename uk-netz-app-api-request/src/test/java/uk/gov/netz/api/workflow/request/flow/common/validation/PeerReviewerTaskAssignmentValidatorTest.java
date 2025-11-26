package uk.gov.netz.api.workflow.request.flow.common.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.resource.RequestTaskAuthorizationResourceService;
import uk.gov.netz.api.authorization.rules.services.resource.ResourceCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.enumeration.SupportingTaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeerReviewerTaskAssignmentValidatorTest {

    @InjectMocks
    private PeerReviewerTaskAssignmentValidator peerReviewerTaskAssignmentValidator;

    @Mock
    private RequestTaskAuthorizationResourceService requestTaskAuthorizationResourceService;

    @Test
    void validate() {
        AppUser appUser = AppUser.builder().userId("userId").roleType(RoleTypeConstants.REGULATOR).build();
        String selectedAssignee = "selectedAssignee";
        RequestTaskType requestTaskType = RequestTaskType.builder().code("requestTaskType1").build();
        Request request = Request.builder().build();
        addResourcesToRequest(1L, CompetentAuthorityEnum.ENGLAND, request);
        RequestTask requestTask = RequestTask.builder().type(requestTaskType).request(request).build();
        RequestTaskType requestTaskTypeToBeAssigned = RequestTaskType.builder().code("requestTaskType2").supporting(SupportingTaskType.PEER_REVIEW).build();

        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
        		.requestResources(Map.of(
                		ResourceType.ACCOUNT, request.getAccountId().toString(),
                		ResourceType.CA, request.getCompetentAuthority().name()
                		))
                .build();

        List<String> users = new ArrayList<>();
        users.add(selectedAssignee);
        users.add(appUser.getUserId());
        when(requestTaskAuthorizationResourceService.
                findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(requestTaskTypeToBeAssigned.getCode(), resourceCriteria, appUser.getRoleType()))
            .thenReturn(users);

        peerReviewerTaskAssignmentValidator.validate(requestTask, requestTaskTypeToBeAssigned, selectedAssignee, appUser);
    }

    @Test
    void validate_assignment_not_allowed() {
        AppUser appUser = AppUser.builder().userId("userId").roleType(RoleTypeConstants.REGULATOR).build();
        String selectedAssignee = "selectedAssignee";
        RequestTaskType requestTaskType = RequestTaskType.builder().code("requestTaskType1").build();
        Request request = Request.builder().build();
        addResourcesToRequest(1L, CompetentAuthorityEnum.ENGLAND, request);
        RequestTask requestTask = RequestTask.builder().type(requestTaskType).request(request).build();
        RequestTaskType requestTaskTypeToBeAssigned = RequestTaskType.builder().code("requestTaskType2").supporting(SupportingTaskType.PEER_REVIEW).build();

        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
        		.requestResources(Map.of(
                		ResourceType.ACCOUNT, request.getAccountId().toString(),
                		ResourceType.CA, request.getCompetentAuthority().name()
                		))
                .build();

        List<String> users = new ArrayList<>();
        users.add(appUser.getUserId());
        when(requestTaskAuthorizationResourceService.
                findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(requestTaskTypeToBeAssigned.getCode(), resourceCriteria, appUser.getRoleType()))
                .thenReturn(users);


        BusinessException businessException = assertThrows(BusinessException.class,
            () -> peerReviewerTaskAssignmentValidator.validate(requestTask, requestTaskTypeToBeAssigned, selectedAssignee, appUser));

        assertEquals(ErrorCode.ASSIGNMENT_NOT_ALLOWED, businessException.getErrorCode());
    }
    
    private void addResourcesToRequest(Long accountId, CompetentAuthorityEnum competentAuthority, Request request) {
		RequestResource caResource = RequestResource.builder()
				.resourceType(ResourceType.CA)
				.resourceId(competentAuthority.name())
				.request(request)
				.build();
		
		RequestResource accountResource = RequestResource.builder()
				.resourceType(ResourceType.ACCOUNT)
				.resourceId(accountId.toString())
				.request(request)
				.build();

        request.getRequestResources().addAll(List.of(caResource, accountResource));
	}
}