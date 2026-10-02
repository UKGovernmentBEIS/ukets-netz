package uk.gov.netz.api.workflow.request.application.filedocument.preview.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.application.filedocument.preview.domain.PreviewDocumentRequest;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.TestRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.common.domain.DecisionNotification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AsyncPreviewDocumentHandlerDelegatorTest {

    @Mock
    private AsyncPreviewDocumentHandler asyncPreviewDocumentHandler;

    @BeforeEach
    void setUp() {
        asyncPreviewDocumentHandler = new AsyncPreviewDocumentHandler() {
            @Override
            public RequestTaskPayload previewDocumentAsync(Long taskId, DecisionNotification decisionNotification) {
                return TestRequestTaskPayload.builder().build();
            }

            @Override
            public List<String> getTypes() {
                return List.of("DUMMY_TYPE");
            }
        };
    }

    @Test
    void triggerDocumentGeneration() {
        List<AsyncPreviewDocumentHandler> handlers = List.of(asyncPreviewDocumentHandler);

        AsyncPreviewDocumentHandlerDelegator requestCreateActionHandlerMapper = new AsyncPreviewDocumentHandlerDelegator(handlers);
        RequestTaskPayload requestTaskPayload = requestCreateActionHandlerMapper
            .triggerDocumentGeneration(1L, PreviewDocumentRequest.builder().documentType("DUMMY_TYPE").build());

        assertThat(requestTaskPayload).isEqualTo(TestRequestTaskPayload.builder().build());
    }

    @Test
    void triggerDocumentGeneration_throws_error_for_invalid_type() {
        List<AsyncPreviewDocumentHandler> handlers = List.of(asyncPreviewDocumentHandler);

        AsyncPreviewDocumentHandlerDelegator requestCreateActionHandlerMapper = new AsyncPreviewDocumentHandlerDelegator(handlers);
        BusinessException exception = assertThrows(BusinessException.class, () -> requestCreateActionHandlerMapper
            .triggerDocumentGeneration(1L, PreviewDocumentRequest.builder().documentType("invalid type").build()));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }
}

