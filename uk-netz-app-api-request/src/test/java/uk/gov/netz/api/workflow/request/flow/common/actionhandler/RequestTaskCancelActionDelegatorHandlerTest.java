package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskPayloadTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.domain.RequestTaskActionEmptyPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentMakeRequestTaskPayload;

@ExtendWith(MockitoExtension.class)
class RequestTaskCancelActionDelegatorHandlerTest {

	@InjectMocks
    private RequestTaskCancelActionDelegatorHandler cut;

    @Mock
    private RequestTaskService requestTaskService;
    
    @Mock
    private RequestTaskCancelActionHandlerResolver requestTaskCancelActionHandlerResolver;
    
    @Test
    void process() {
    	Long requestTaskId = 1L;
    	String requestTaskActionType = "CANCEL_APPLICATION";
    	AppUser appUser = AppUser.builder().userId("userId").build();
    	RequestTaskActionEmptyPayload payload = RequestTaskActionEmptyPayload.builder().build();
    	
    	RequestTaskType requestTaskType = RequestTaskType.builder().code("DUMMY_REQUEST_TYPE").build();

		final RequestTaskPayload requestTaskPayload =
				PaymentMakeRequestTaskPayload.builder()
						.payloadType("DUMMY_REQUEST_PAYLOAD_TYPE")
						.build();
    	
    	RequestTask requestTask = RequestTask.builder()
    			.type(requestTaskType)
				.payload(requestTaskPayload)
    			.build();
    	
		RequestTaskCancelActionHandler requestTaskCancelActionHandler = Mockito
				.mock(RequestTaskCancelActionHandler.class);
    	
    	when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
		when(requestTaskCancelActionHandlerResolver.resolve(requestTaskType.getCode()))
				.thenReturn(requestTaskCancelActionHandler);

		RequestTaskPayload taskPayload = cut.process(requestTaskId, requestTaskActionType, appUser, payload);

		assertThat(taskPayload).isEqualTo(requestTaskPayload);
		verify(requestTaskService, times(1)).findTaskById(requestTaskId);
		verify(requestTaskCancelActionHandlerResolver, times(1)).resolve(requestTaskType.getCode());
		verify(requestTaskCancelActionHandler, times(1)).cancel(requestTaskId, payload, appUser);
    }
    
    @Test
    void getTypes() {
    	assertThat(cut.getTypes()).contains(RequestTaskActionTypes.CANCEL_APPLICATION);
    }
    
}
