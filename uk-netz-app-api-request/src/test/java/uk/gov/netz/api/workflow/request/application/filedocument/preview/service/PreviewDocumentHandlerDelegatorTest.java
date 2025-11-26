package uk.gov.netz.api.workflow.request.application.filedocument.preview.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.workflow.request.application.filedocument.preview.domain.PreviewDocumentRequest;
import uk.gov.netz.api.workflow.request.flow.common.domain.DecisionNotification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PreviewDocumentHandlerDelegatorTest {
    private static final byte[] FILE = "test".getBytes();

    @Mock
    private PreviewDocumentHandler previewDocumentHandler;

    @BeforeEach
    void setUp() {
        previewDocumentHandler = new PreviewDocumentHandler() {
            @Override
            public FileDTO previewDocument(Long taskId, DecisionNotification decisionNotification) {
                return FileDTO.builder().fileContent(FILE).build();
            }

            @Override
            public List<String> getTypes() {
                return List.of("DUMMY_TYPE");
            }
        };
    }

    @Test
    void getDocument() {
        List<PreviewDocumentHandler> handlers = List.of(previewDocumentHandler);

        PreviewDocumentHandlerDelegator requestCreateActionHandlerMapper = new PreviewDocumentHandlerDelegator(handlers);
        FileDTO fileDTO = requestCreateActionHandlerMapper
            .getDocument(1L, PreviewDocumentRequest.builder().documentType("DUMMY_TYPE").build());

        assertThat(fileDTO).isEqualTo(FileDTO.builder().fileContent(FILE).build());
    }

    @Test
    void getDocument_throws_error_for_invalid_type() {
        List<PreviewDocumentHandler> handlers = List.of(previewDocumentHandler);

        PreviewDocumentHandlerDelegator requestCreateActionHandlerMapper = new PreviewDocumentHandlerDelegator(handlers);
        BusinessException exception = assertThrows(BusinessException.class, () -> requestCreateActionHandlerMapper
            .getDocument(1L, PreviewDocumentRequest.builder().documentType("invalid type").build()));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
