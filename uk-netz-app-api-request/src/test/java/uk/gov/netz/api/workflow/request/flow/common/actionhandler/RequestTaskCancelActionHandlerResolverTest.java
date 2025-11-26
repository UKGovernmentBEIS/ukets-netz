package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionPayload;

@ExtendWith(MockitoExtension.class)
public class RequestTaskCancelActionHandlerResolverTest {

	@InjectMocks
    private RequestTaskCancelActionHandlerResolver cut;
	
	@Mock
    private TestRequestTaskCancelActionHandler testRequestTaskCancelActionHandler;

    @Spy
    private ArrayList<RequestTaskCancelActionHandler> handlers;

    @BeforeEach
    public void setUp() {
    	handlers.add(testRequestTaskCancelActionHandler);
    }
    
    @Test
    void resolve_contains() {
    	String requestTaskType = "REQUEST_TASK_TYPE";
    	
    	when(testRequestTaskCancelActionHandler.getRequestTaskTypes()).thenReturn(List.of(
    			requestTaskType
    			));
    	
    	RequestTaskCancelActionHandler result = cut.resolve(requestTaskType);
    	assertThat(result).isEqualTo(testRequestTaskCancelActionHandler);
    	
    	verify(testRequestTaskCancelActionHandler, times(1)).getRequestTaskTypes();
    }

    @Test
    void resolve_ends_with() {
    	String subflowRequestTaskType = "WAIT_FOR_RFI_RESPONSE";
    	String requestTaskType = "FLOW_WAIT_" + subflowRequestTaskType;
    	
    	when(testRequestTaskCancelActionHandler.getRequestTaskTypes()).thenReturn(List.of(
    			subflowRequestTaskType
    			));
    	
    	RequestTaskCancelActionHandler result = cut.resolve(requestTaskType);
    	assertThat(result).isEqualTo(testRequestTaskCancelActionHandler);
    	
    	verify(testRequestTaskCancelActionHandler, times(2)).getRequestTaskTypes();
    }
    
    @Test
    void resolve_not_found() {
    	String requestTaskType = "REQUEST_TASK_TYPE";
    	
    	when(testRequestTaskCancelActionHandler.getRequestTaskTypes()).thenReturn(List.of(
    			requestTaskType
    			));
    	
    	BusinessException be = assertThrows(BusinessException.class, () -> cut.resolve("ANOTHER"));
    	assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    	
    	verify(testRequestTaskCancelActionHandler, times(2)).getRequestTaskTypes();
    }

	private static class TestRequestTaskCancelActionHandler implements RequestTaskCancelActionHandler {
		@Override
		public void cancel(Long requestTaskId, RequestTaskActionPayload payload, AppUser appUser) {

		}
		@Override
		public List getRequestTaskTypes() {
			return null;
		}
	}
}
