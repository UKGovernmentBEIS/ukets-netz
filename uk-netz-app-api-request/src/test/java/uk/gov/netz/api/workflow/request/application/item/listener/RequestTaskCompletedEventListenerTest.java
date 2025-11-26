package uk.gov.netz.api.workflow.request.application.item.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.workflow.request.application.item.service.RequestTaskVisitService;
import uk.gov.netz.api.workflow.request.application.taskcompleted.RequestTaskCompletedEvent;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestTaskAttachmentsUncoupleService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class RequestTaskCompletedEventListenerTest {

    @InjectMocks
    private RequestTaskCompletedEventListener listener;
    
    @Mock
    private RequestTaskVisitService requestTaskVisitService;

    @Mock
    private RequestTaskAttachmentsUncoupleService requestTaskAttachmentsUncoupleService;
    
    @Test
    void onRequestTaskCompletedEvent() {
        RequestTaskCompletedEvent event = RequestTaskCompletedEvent.builder().requestTaskId(1L).build();
        listener.onRequestTaskCompletedEvent(event);
        verify(requestTaskVisitService, times(1)).deleteByTaskId(event.getRequestTaskId());
        verify(requestTaskAttachmentsUncoupleService, times(1)).uncoupleAttachments(1L);
    }
}
