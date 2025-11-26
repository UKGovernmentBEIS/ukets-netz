package uk.gov.netz.api.workflow.request.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.resource.AccountRequestAuthorizationResourceService;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestCreateByAccountValidator;

@ExtendWith(MockitoExtension.class)
class AvailableRequestAccountHandlerTest {

	private AvailableRequestAccountHandler availableRequestAccountHandler;

    @Mock
    private AccountRequestAuthorizationResourceService accountRequestAuthorizationResourceService;

    @Mock
    private AvailableRequestAccountHandlerTest.TestRequestCreateValidatorA requestCreateValidatorA;

    @Mock
    private AvailableRequestAccountHandlerTest.TestRequestCreateValidatorB requestCreateValidatorB;

    @Mock
    private AccountQueryService accountQueryService;
    
    @BeforeEach
    public void setUp() {
        ArrayList<RequestCreateByAccountValidator> requestCreateByAccountValidators = new ArrayList<>();
        requestCreateByAccountValidators.add(requestCreateValidatorA);
        requestCreateByAccountValidators.add(requestCreateValidatorB);

        availableRequestAccountHandler = new AvailableRequestAccountHandler(
        		accountRequestAuthorizationResourceService, requestCreateByAccountValidators);
    }
    
    @Test
    void getAvailableRequestsForResource() {
        final AppUser user = AppUser.builder().userId("user").build();
        final String resourceId = "1";
        final long accountId = 1L;
        final RequestCreateValidationResult result = RequestCreateValidationResult.builder().valid(true).build();
        
        final Set<String> allManuallyCreateCreateRequestTypes = Set.of("code1", "code2", "code3");
        
        when(accountRequestAuthorizationResourceService.findRequestCreateActionsByAccountId(user, accountId))
        	.thenReturn(Set.of("code1"));
        when(requestCreateValidatorA.getRequestType()).thenReturn("code1");
        when(requestCreateValidatorA.validateAction(accountId)).thenReturn(result);
        
        // Invoke
        final Map<String, RequestCreateValidationResult> availableWorkflows =
        		availableRequestAccountHandler.getAvailableRequestsForResource(resourceId, allManuallyCreateCreateRequestTypes, user);

        // Verify
        verify(accountRequestAuthorizationResourceService, times(1))
        		.findRequestCreateActionsByAccountId(user, accountId);
        verify(requestCreateValidatorA, times(1)).getRequestType();
        verify(requestCreateValidatorA, times(1)).validateAction(accountId);

        assertThat(availableWorkflows).containsExactly(Map.entry("code1", result));
    }
    
    private static class TestRequestCreateValidatorA implements RequestCreateByAccountValidator {

        @Override
        public RequestCreateValidationResult validateAction(Long accountId) {
            return null;
        }

        @Override
        public String getRequestType() {
            return null;
        }
    }

    private static class TestRequestCreateValidatorB implements RequestCreateByAccountValidator {

        @Override
        public RequestCreateValidationResult validateAction(Long accountId) {
            return null;
        }

        @Override
        public String getRequestType() {
            return null;
        }
    }
}
