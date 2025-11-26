package uk.gov.netz.api.workflow.request.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.repository.RequestTypeRepository;
import uk.gov.netz.api.workflow.request.core.validation.EnabledWorkflowValidator;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailableRequestServiceTest {
    
    @Mock
    private RequestTypeRepository requestTypeRepository;
    
    @Mock
    private AvailableRequestResourceTypeHandler availableRequestResourceTypeHandler;
    
    @Mock
    private EnabledWorkflowValidator enabledWorkflowValidator;

    @Test
    void getAvailableWorkflows() {
    	final RequestCreateValidationResult result = RequestCreateValidationResult.builder().valid(true).build();
    	availableRequestResourceTypeHandler = new AvailableRequestResourceTypeHandler() {

            @Override
            public String getResourceType() {
                return "ACCOUNT";
            }

			@Override
			public Map<String, RequestCreateValidationResult> getAvailableRequestsForResource(
					String resourceId, Set<String> requestTypes, AppUser appUser) {
				return Map.of("code1", result);
			}
        };
        
        final AppUser user = AppUser.builder().userId("user").build();
        final String resourceId = "1";
        final String resourceType = "ACCOUNT";
        final List<AvailableRequestResourceTypeHandler> handlers = List.of(availableRequestResourceTypeHandler);
        
        final Set<RequestType> allManuallyCreateCreateRequestTypes = new HashSet<>();
        allManuallyCreateCreateRequestTypes.add(RequestType.builder()
        		.code("code1")
        		.canCreateManually(true)
        		.resourceType("ACCOUNT")
        		.build());
        allManuallyCreateCreateRequestTypes.add(RequestType.builder()
        		.code("code2")
        		.canCreateManually(true)
        		.resourceType("ACCOUNT")
        		.build());
        allManuallyCreateCreateRequestTypes.add(RequestType.builder()
        		.code("code3")
        		.canCreateManually(true)
        		.resourceType("ACCOUNT")
        		.build());
        
        when(requestTypeRepository.findAllByCanCreateManuallyAndResourceType(true, ResourceType.ACCOUNT))
        		.thenReturn(allManuallyCreateCreateRequestTypes);
        when(enabledWorkflowValidator.isWorkflowEnabled("code1")).thenReturn(true);
        when(enabledWorkflowValidator.isWorkflowEnabled("code2")).thenReturn(false);
        when(enabledWorkflowValidator.isWorkflowEnabled("code3")).thenReturn(false);
        
        // Invoke
        AvailableRequestService availableRequestService = 
        		new AvailableRequestService(handlers, requestTypeRepository, enabledWorkflowValidator);
        final Map<String, RequestCreateValidationResult> availableWorkflows =
                availableRequestService.getAvailableWorkflows(resourceId, resourceType, user);

        // Verify
        verify(requestTypeRepository, times(1)).findAllByCanCreateManuallyAndResourceType(true, ResourceType.ACCOUNT);
        verify(enabledWorkflowValidator, times(1)).isWorkflowEnabled("code1");
        verify(enabledWorkflowValidator, times(1)).isWorkflowEnabled("code2");
        verify(enabledWorkflowValidator, times(1)).isWorkflowEnabled("code3");

        assertThat(availableWorkflows).containsExactly(Map.entry("code1", result));
    }
}
