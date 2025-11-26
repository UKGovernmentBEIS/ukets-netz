package uk.gov.netz.api.workflow.request.flow.rfi.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.flow.rfi.service.RfiUploadAttachmentService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class RfiUploadAttachmentHandlerTest {

    @InjectMocks
    private RfiUploadAttachmentHandler handler;

    @Mock
    private RfiUploadAttachmentService uploadAttachmentService;

    @Test
    void uploadAttachment() {

        final Long requestTaskId = 1L;
        final String filename = "filename";
        final String attachmentUuid = UUID.randomUUID().toString();

        handler.uploadAttachment(requestTaskId, attachmentUuid, filename);

        verify(uploadAttachmentService, times(1)).uploadAttachment(requestTaskId, attachmentUuid, filename);
    }

    @Test
    void getTypes() {
        assertThat(handler.getType()).isEqualTo(RequestTaskActionTypes.RFI_UPLOAD_ATTACHMENT);
    }
}
