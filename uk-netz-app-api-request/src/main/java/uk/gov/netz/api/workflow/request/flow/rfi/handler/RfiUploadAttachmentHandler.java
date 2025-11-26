package uk.gov.netz.api.workflow.request.flow.rfi.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.flow.common.actionhandler.RequestTaskUploadAttachmentActionHandler;
import uk.gov.netz.api.workflow.request.flow.rfi.service.RfiUploadAttachmentService;

@Component
@RequiredArgsConstructor
public class RfiUploadAttachmentHandler extends RequestTaskUploadAttachmentActionHandler {

    private final RfiUploadAttachmentService rfiUploadAttachmentService;
    
    @Override
    public void uploadAttachment(Long requestTaskId, String attachmentUuid, String filename) {
        rfiUploadAttachmentService.uploadAttachment(requestTaskId, attachmentUuid, filename);
    }

    @Override
    public String getType() {
        return RequestTaskActionTypes.RFI_UPLOAD_ATTACHMENT;
    }
}
