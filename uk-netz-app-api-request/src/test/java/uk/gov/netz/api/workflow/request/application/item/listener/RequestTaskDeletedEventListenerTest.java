package uk.gov.netz.api.workflow.request.application.item.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.workflow.request.application.item.service.RequestTaskVisitService;
import uk.gov.netz.api.workflow.request.application.taskdeleted.RequestTaskDeletedEvent;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestTaskAttachmentsUncoupleService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class RequestTaskDeletedEventListenerTest {

    @InjectMocks
    private RequestTaskDeletedEventListener listener;

    @Mock
    private RequestTaskVisitService requestTaskVisitService;

    @Mock
    private RequestTaskAttachmentsUncoupleService requestTaskAttachmentsUncoupleService;

    @Test
    void onRequestTaskDeletedEvent() {
        
        final RequestTaskDeletedEvent event = RequestTaskDeletedEvent.builder().requestTaskId(1L).build();
        listener.onRequestTaskDeletedEvent(event);
        verify(requestTaskVisitService, times(1)).deleteByTaskId(event.getRequestTaskId());
        verify(requestTaskAttachmentsUncoupleService, times(1)).deletePendingAttachments(1L);
    }
}
