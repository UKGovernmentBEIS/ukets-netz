package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.TestRequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionType;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.core.validation.RequestTaskActionValidatorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

@ExtendWith(MockitoExtension.class)
class ProcessRequestTaskAspectTest {

    @InjectMocks
    private ProcessRequestTaskAspect processRequestTaskAspect;

    @Mock
    private RequestTaskService requestTaskService;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private RequestTaskActionValidatorService requestTaskActionValidatorService;

    @Test
    void validateProcessRequestTask() {
        final AppUser user = AppUser.builder().userId("userId").build();
        final Object[] arguments = new Object[]{1L,
                "PAYMENT_PAY_BY_CARD", user, TestRequestTaskActionPayload.builder().build()};
        final RequestTask requestTask = RequestTask.builder()
                .assignee("userId").type(RequestTaskType.builder().code("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW").actionTypes(Set.of(
                		RequestTaskActionType.builder().code("PAYMENT_PAY_BY_CARD").build()
                		)).build()).build();

        // Mock
        when(joinPoint.getArgs()).thenReturn(arguments);
        when(requestTaskService.findTaskById(1L)).thenReturn(requestTask);
        doNothing().when(requestTaskActionValidatorService).validate(requestTask,
                "PAYMENT_PAY_BY_CARD");

        // Invoke
        processRequestTaskAspect.validateProcessRequestTask(joinPoint);

        // Assert
        verify(requestTaskService, times(1)).findTaskById(1L);
        verify(requestTaskActionValidatorService, times(1)).validate(requestTask,
                "PAYMENT_PAY_BY_CARD");
    }

    @Test
    void validateProcessRequestTask_not_valid_assignee() {
        final AppUser user = AppUser.builder().userId("userId").build();
        final Object[] arguments = new Object[]{1L,
                "PAYMENT_MARK_AS_RECEIVED", user, TestRequestTaskActionPayload.builder().build()};
        final RequestTask requestTask = RequestTask.builder()
                .assignee("userId2").type(RequestTaskType.builder().code("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW").build()).build();

        // Mock
        when(joinPoint.getArgs()).thenReturn(arguments);
        when(requestTaskService.findTaskById(1L)).thenReturn(requestTask);

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class,
                () -> processRequestTaskAspect.validateProcessRequestTask(joinPoint));

        // Assert
        assertEquals(ErrorCode.REQUEST_TASK_ACTION_USER_NOT_THE_ASSIGNEE, businessException.getErrorCode());
        verify(requestTaskService, times(1)).findTaskById(1L);
    }

    @Test
    void validateProcessRequestTask_not_valid_action() {
        final AppUser user = AppUser.builder().userId("userId").build();
        final Object[] arguments = new Object[]{1L,
            "PAYMENT_CANCEL", user, TestRequestTaskActionPayload.builder().build()};
        final RequestTask requestTask = RequestTask.builder()
                .assignee("userId").type(RequestTaskType.builder().code("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW").build()).build();

        // Mock
        when(joinPoint.getArgs()).thenReturn(arguments);
        when(requestTaskService.findTaskById(1L)).thenReturn(requestTask);

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class,
                () -> processRequestTaskAspect.validateProcessRequestTask(joinPoint));

        // Assert
        assertEquals(ErrorCode.REQUEST_TASK_ACTION_CANNOT_PROCEED, businessException.getErrorCode());
        verify(requestTaskService, times(1)).findTaskById(1L);
    }

    @Test
    void validateProcessRequestTask_no_task_exists() {
        final AppUser user = AppUser.builder().userId("userId").build();
        final Object[] arguments = new Object[]{1L,
                "PAYMENT_MARK_AS_RECEIVED", user, TestRequestTaskActionPayload.builder().build()};

        // Mock
        when(joinPoint.getArgs()).thenReturn(arguments);
        when(requestTaskService.findTaskById(1L)).thenThrow(new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class,
                () -> processRequestTaskAspect.validateProcessRequestTask(joinPoint));

        // Assert
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, businessException.getErrorCode());
        verify(requestTaskService, times(1)).findTaskById(1L);
    }

    @Test
    void validateProcessRequestTask_whenCustomValidationFails_thenThrowException() {
        
        final AppUser user = AppUser.builder().userId("userId").build();
        final Object[] arguments = new Object[]{1L,
                "PAYMENT_PAY_BY_CARD", user,
                TestRequestTaskActionPayload.builder().build()};
        final RequestTask requestTask = RequestTask.builder()
                .assignee("userId").type(RequestTaskType.builder().code("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW").actionTypes(Set.of(
                		RequestTaskActionType.builder().code("PAYMENT_PAY_BY_CARD").build()
                		)).build()).build();

        // Mock
        when(joinPoint.getArgs()).thenReturn(arguments);
        when(requestTaskService.findTaskById(1L)).thenReturn(requestTask);
        doThrow(new BusinessException(ErrorCode.REQUEST_TASK_ACTION_CANNOT_PROCEED))
            .when(requestTaskActionValidatorService)
            .validate(requestTask, "PAYMENT_PAY_BY_CARD");
        
        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class,
            () -> processRequestTaskAspect.validateProcessRequestTask(joinPoint));

        // Assert
        assertEquals(ErrorCode.REQUEST_TASK_ACTION_CANNOT_PROCEED, businessException.getErrorCode());
    }
}
