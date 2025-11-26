package uk.gov.netz.api.workflow.request.flow.rfi.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestTaskAttachmentsUncoupleService;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RfiResponseSubmitInitializerTest {

    @InjectMocks
    private RfiResponseSubmitInitializer initializer;

    @Mock
    private RequestTaskAttachmentsUncoupleService uncoupleService;
    
    @Mock
    private RequestTaskTypeRepository requestTaskTypeRepository;

    @Test
    void getRequestTaskTypes() {
    	when(requestTaskTypeRepository.findAllByCodeEndingWith(RequestTaskTypes.RFI_RESPONSE_SUBMIT))
			.thenReturn(Set.of(RequestTaskType.builder().code("1REQUEST_TASK_RFI_RESPONSE_SUBMIT").build(),
				RequestTaskType.builder().code("2REQUEST_TASK_RFI_RESPONSE_SUBMIT").build()));

		assertEquals(initializer.getRequestTaskTypes(),
				Set.of("1REQUEST_TASK_RFI_RESPONSE_SUBMIT", "2REQUEST_TASK_RFI_RESPONSE_SUBMIT"));
		
		verify(requestTaskTypeRepository, times(1)).findAllByCodeEndingWith(RequestTaskTypes.RFI_RESPONSE_SUBMIT);
    }
}
