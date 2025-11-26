package uk.gov.netz.api.workflow.request.flow.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;
import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.service.RequestQueryService;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestCreateValidatorServiceTest {

    @InjectMocks
    private RequestCreateValidatorService validatorService;

    @Mock
    private AccountQueryService accountQueryService;

    @Mock
    private RequestQueryService requestQueryService;

    @Test
    void validate() {
        final Long accountId = 1L;
        final Set<AccountStatus> applicableAccountStatuses = Set.of(
                TestAccountStatus.DUMMY,
                TestAccountStatus.DUMMY2
        );
        final Set<String> mutuallyExclusiveRequestsTypes = Set.of("DUMMY_REQUEST_TYPE");

        when(accountQueryService.getAccountStatus(accountId)).thenReturn(TestAccountStatus.DUMMY);
        when(requestQueryService.findInProgressRequestsByAccount(accountId)).thenReturn(
                List.of(Request.builder().type(RequestType.builder().code("another").build()).build())
        );

        // Invoke
        RequestCreateValidationResult result = validatorService
                .validate(accountId, applicableAccountStatuses, mutuallyExclusiveRequestsTypes);

        // Verify
        assertThat(result).isEqualTo(RequestCreateValidationResult.builder().valid(true).build());
        verify(accountQueryService, times(1)).getAccountStatus(accountId);
        verify(requestQueryService, times(1)).findInProgressRequestsByAccount(accountId);
    }

    @Test
    void validate_failed() {
        final Long accountId = 1L;
        final Set<AccountStatus> applicableAccountStatuses = Set.of(
        		TestAccountStatus.DUMMY
        );

        when(accountQueryService.getAccountStatus(accountId)).thenReturn(TestAccountStatus.DUMMY2);

        // Invoke
        RequestCreateValidationResult result = validatorService
                .validate(accountId, applicableAccountStatuses, Set.of());

        // Verify
        assertThat(result).isEqualTo(RequestCreateValidationResult.builder().valid(false)
                .reportedAccountStatus(TestAccountStatus.DUMMY2.getName())
                .applicableAccountStatuses(Set.of(
                		TestAccountStatus.DUMMY.getName()
                )).build());

        verify(accountQueryService, times(1)).getAccountStatus(accountId);
        verify(requestQueryService, never()).findInProgressRequestsByAccount(anyLong());
    }

    @Test
    void validate_whenConflicts_thenFail() {
        final Long accountId = 1L;
        final Set<AccountStatus> applicableAccountStatuses = Set.of(
        		TestAccountStatus.DUMMY,
        		TestAccountStatus.DUMMY2
        );
        final Set<String> mutuallyExclusiveRequestsTypes = Set.of("DUMMY_REQUEST_TYPE");

        when(accountQueryService.getAccountStatus(accountId)).thenReturn(TestAccountStatus.DUMMY);
        when(requestQueryService.findInProgressRequestsByAccount(accountId)).thenReturn(
                List.of(Request.builder().type(RequestType.builder().code("DUMMY_REQUEST_TYPE").build()).build())
        );

        // Invoke
        RequestCreateValidationResult result = validatorService
                .validate(accountId, applicableAccountStatuses, mutuallyExclusiveRequestsTypes);

        // Verify
        assertThat(result).isEqualTo(RequestCreateValidationResult.builder().valid(false)
                .reportedRequestTypes(Set.of("DUMMY_REQUEST_TYPE")).build());

        verify(accountQueryService, times(1)).getAccountStatus(accountId);
        verify(requestQueryService, times(1)).findInProgressRequestsByAccount(accountId);
    }
}
