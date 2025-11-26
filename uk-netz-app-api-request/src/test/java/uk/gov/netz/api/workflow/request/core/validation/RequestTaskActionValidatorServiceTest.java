package uk.gov.netz.api.workflow.request.core.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionValidationResult;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionValidationErrorCodes;

import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestTaskActionValidatorServiceTest {

    @InjectMocks
    private RequestTaskActionValidatorService requestTaskActionValidatorService;

    @Spy
    private ArrayList<RequestTaskActionValidator> requestTaskActionValidators;

    @Mock
    private PaymentPendingRequestTaskActionValidator paymentPendingRequestTaskActionValidator;

    @BeforeEach
    void setUp() {
        requestTaskActionValidators.add(paymentPendingRequestTaskActionValidator);
    }

    @Test
    void validate() {
        RequestTask requestTask = RequestTask.builder().build();
        String taskActionType = "taskActionType";
        RequestTaskActionValidationResult validationResult = RequestTaskActionValidationResult.builder().valid(true).build();

        when(paymentPendingRequestTaskActionValidator.getTypes()).thenReturn(Set.of(taskActionType));
        when(paymentPendingRequestTaskActionValidator.validate(requestTask)).thenReturn(validationResult);

        requestTaskActionValidatorService.validate(requestTask, taskActionType);
    }

    @Test
    void validate_invalid() {
        RequestTask requestTask = RequestTask.builder().build();
        String taskActionType = "taskActionType";
        RequestTaskActionValidationResult validationResult = RequestTaskActionValidationResult.builder()
            .valid(false)
            .errorCode(RequestTaskActionValidationErrorCodes.PAYMENT_IN_PROGRESS)
            .build();

        when(paymentPendingRequestTaskActionValidator.getTypes()).thenReturn(Set.of(taskActionType));
        when(paymentPendingRequestTaskActionValidator.validate(requestTask)).thenReturn(validationResult);

        BusinessException businessException = assertThrows(BusinessException.class,
            () -> requestTaskActionValidatorService.validate(requestTask, taskActionType));


        assertEquals(ErrorCode.REQUEST_TASK_ACTION_CANNOT_PROCEED, businessException.getErrorCode());

        Object[] errors = businessException.getData();
        assertThat(errors).containsOnly(RequestTaskActionValidationErrorCodes.PAYMENT_IN_PROGRESS);
    }

    @Test
    void validate_no_validator_matched() {
        RequestTask requestTask = RequestTask.builder().build();
        String taskActionType = "taskActionType";

        when(paymentPendingRequestTaskActionValidator.getTypes()).thenReturn(Set.of(taskActionType));

        requestTaskActionValidatorService.validate(requestTask, "another");

        verify(paymentPendingRequestTaskActionValidator, never()).validate(any());
    }
}