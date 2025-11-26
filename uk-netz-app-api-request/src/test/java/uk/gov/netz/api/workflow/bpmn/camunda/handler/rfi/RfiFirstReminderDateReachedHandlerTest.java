package uk.gov.netz.api.workflow.bpmn.camunda.handler.rfi;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.rfi.service.RfiSendReminderNotificationService;

import java.util.Date;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RfiFirstReminderDateReachedHandlerTest {

    @InjectMocks
    private RfiFirstReminderDateReachedHandler handler;
    
    @Mock
    private RfiSendReminderNotificationService rfiSendReminderNotificationService;
    
    @Test
    void execute() {
        final DelegateExecution execution = spy(DelegateExecution.class);
        final String requestId = "1";
        final Date expirationDate = new Date();
        when(execution.getVariable(BpmnProcessConstants.REQUEST_ID)).thenReturn(requestId);
        when(execution.getVariable(BpmnProcessConstants.RFI_EXPIRATION_DATE)).thenReturn(expirationDate);
        
        handler.execute(execution);
        
        verify(execution, times(1)).getVariable(BpmnProcessConstants.REQUEST_ID);
        verify(execution, times(1)).getVariable(BpmnProcessConstants.RFI_EXPIRATION_DATE);
        verify(rfiSendReminderNotificationService, times(1)).sendFirstReminderNotification(requestId, expirationDate);
    }
}
