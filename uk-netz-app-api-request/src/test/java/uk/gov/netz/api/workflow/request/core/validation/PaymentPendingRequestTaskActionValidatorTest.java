package uk.gov.netz.api.workflow.request.core.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskActionTypeRepository;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PaymentPendingRequestTaskActionValidatorTest {

    @InjectMocks
    private PaymentPendingRequestTaskActionValidator validator;

    @Mock
    private RequestTaskActionTypeRepository requestTaskActionTypeRepository;
    
    @Test
    void getTypes() {
    	when(requestTaskActionTypeRepository.findAllByBlockedByPayment(true)).thenReturn(Set.of(
    			RequestTaskActionType.builder().code("code1").blockedByPayment(true).build()
    			));
    	
        Set<String> requestTaskActionTypes = new HashSet<>();
		requestTaskActionTypes.addAll(Set.of(RequestTaskActionTypes.RFI_SUBMIT, RequestTaskActionTypes.RDE_SUBMIT, "code1"));

        assertEquals(requestTaskActionTypes, validator.getTypes());
        verify(requestTaskActionTypeRepository, times(1)).findAllByBlockedByPayment(true);
    }

}