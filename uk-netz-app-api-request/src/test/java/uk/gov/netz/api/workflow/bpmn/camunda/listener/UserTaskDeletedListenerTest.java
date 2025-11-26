package uk.gov.netz.api.workflow.bpmn.camunda.listener;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.workflow.request.flow.common.taskhandler.DynamicUserTaskDeletedHandlerResolver;
import uk.gov.netz.api.workflow.request.application.taskdeleted.RequestTaskDeleteService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserTaskDeletedListenerTest {

    @InjectMocks
    private UserTaskDeletedListener userTaskDeletedListener;
    
    @Mock
    private RequestTaskDeleteService requestTaskDeleteService;
    
    @Mock
    private DynamicUserTaskDeletedHandlerResolver dynamicUserTaskDeletedHandlerMapper;
        
    @Mock
    private DelegateTask taskDelegate;
    
    @Test
    void onTaskDeletedEvent_whenNoHandlerExists_thenDefaultDelete() {
        final String processTaskId ="taskid";
        when(taskDelegate.getId()).thenReturn(processTaskId);
        
        //invoke
        userTaskDeletedListener.onTaskDeletedEvent(taskDelegate);
        
        //verify
        verify(taskDelegate, times(1)).getId();
        verify(requestTaskDeleteService, times(1)).delete(processTaskId);
    }
}
