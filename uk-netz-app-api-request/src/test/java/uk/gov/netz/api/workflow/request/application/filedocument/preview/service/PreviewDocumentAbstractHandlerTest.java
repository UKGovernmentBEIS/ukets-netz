package uk.gov.netz.api.workflow.request.application.filedocument.preview.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.domain.DecisionNotification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreviewDocumentAbstractHandlerTest {
    private static final long REQUEST_TASK_ID = 1L;
    private static final String TASK_CODE = "code";

    @InjectMocks
    private TestPreviewDocumentAbstractHandler testPreviewDocumentAbstractHandler;

    @Mock
    private RequestTaskService requestTaskService;

    @Test
    void previewDocument() {
        RequestTaskType type = RequestTaskType.builder().code(TASK_CODE).build();
        RequestTask requestTask = RequestTask.builder().type(type).build();

        when(requestTaskService.findTaskById(REQUEST_TASK_ID)).thenReturn(requestTask);

        testPreviewDocumentAbstractHandler.previewDocument(REQUEST_TASK_ID, null);

        verify(requestTaskService).findTaskById(REQUEST_TASK_ID);
        verifyNoMoreInteractions(requestTaskService);
    }

    @Test
    void previewDocument_throws_exception_when_code_is_invalid() {
        RequestTaskType type = RequestTaskType.builder().code("invalid code").build();
        RequestTask requestTask = RequestTask.builder().type(type).build();

        when(requestTaskService.findTaskById(REQUEST_TASK_ID)).thenReturn(requestTask);

        BusinessException businessException = assertThrows(BusinessException.class, () ->
            testPreviewDocumentAbstractHandler.previewDocument(REQUEST_TASK_ID, null));

        assertEquals(ErrorCode.INVALID_DOCUMENT_TEMPLATE_FOR_REQUEST_TASK, businessException.getErrorCode());

        verify(requestTaskService).findTaskById(REQUEST_TASK_ID);
        verifyNoMoreInteractions(requestTaskService);
    }

    private static class TestPreviewDocumentAbstractHandler extends PreviewDocumentAbstractHandler {

        public TestPreviewDocumentAbstractHandler(RequestTaskService requestTaskService) {
            super(requestTaskService);
        }

        @Override
        protected List<String> getTaskTypes() {
            return List.of(TASK_CODE);
        }

        @Override
        protected FileDTO generateDocument(Long taskId, DecisionNotification decisionNotification) {
            return null;
        }

        @Override
        public List<String> getTypes() {
            return List.of();
        }
    }
}