package uk.gov.netz.api.workflow.request.application.attachment.requestaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.files.attachments.service.storage.FileAttachmentStorageService;
import uk.gov.netz.api.token.FileToken;
import uk.gov.netz.api.workflow.request.TestRequestActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestAction;
import uk.gov.netz.api.workflow.request.core.repository.RequestActionRepository;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiSubmittedRequestActionPayload;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.common.exception.ErrorCode.RESOURCE_NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class RequestActionFileAttachmentServiceTest {

    @InjectMocks
    private RequestActionFileAttachmentService cut;

    @Mock
    private RequestActionRepository requestActionRepository;

    @Mock
    private FileAttachmentStorageService fileAttachmentStorageService;
    
    @Test
    void generateGetFileAttachmentToken(){
    	Long requestActionId = 1L;
        UUID attachmentUuid = UUID.randomUUID();
        RfiSubmittedRequestActionPayload payload = RfiSubmittedRequestActionPayload
            .builder()
            .rfiAttachments(Map.of(
            		attachmentUuid, "test1"
            		))
            .build();
        RequestAction requestAction = RequestAction.builder().id(requestActionId).payload(payload).build();

        when(requestActionRepository.findById(requestActionId)).thenReturn(Optional.of(requestAction));
        
        FileToken expectedResult = FileToken.builder().build();
        
        when(fileAttachmentStorageService.generateGetFileAttachmentToken(attachmentUuid.toString())).thenReturn(expectedResult);

        FileToken result = cut.generateGetFileAttachmentToken(requestActionId, attachmentUuid);
        assertThat(result).isEqualTo(expectedResult);

        verify(requestActionRepository, times(1)).findById(requestActionId);
        verify(fileAttachmentStorageService, times(1)).generateGetFileAttachmentToken(attachmentUuid.toString());
    }

    @Test
    void generateGetFileAttachmentToken_request_action_not_exists(){
        Long requestActionId = 1L;
        UUID attachmentUuid = UUID.randomUUID();

        when(requestActionRepository.findById(requestActionId)).thenReturn(Optional.empty());

        BusinessException businessException = assertThrows(BusinessException.class, () ->
        cut.generateGetFileAttachmentToken(requestActionId, attachmentUuid));

        assertThat(businessException.getErrorCode()).isEqualTo(RESOURCE_NOT_FOUND);

        verify(requestActionRepository, times(1)).findById(requestActionId);
        verifyNoInteractions(fileAttachmentStorageService);
    }

    @Test
    void generateGetFileAttachmentToken_attachment_not_exists_in_payload() {
        Long requestActionId = 1L;
        UUID attachmentUuid = UUID.randomUUID();
        TestRequestActionPayload payload = TestRequestActionPayload
            .builder()
            .payloadType("REQ_ACTION_PAYLOAD_TYPE")
            .build();
        RequestAction requestAction = RequestAction.builder().id(requestActionId).payload(payload).build();

        when(requestActionRepository.findById(requestActionId)).thenReturn(Optional.of(requestAction));

        BusinessException businessException = assertThrows(BusinessException.class, () ->
        cut.generateGetFileAttachmentToken(requestActionId, attachmentUuid));

        assertThat(businessException.getErrorCode()).isEqualTo(RESOURCE_NOT_FOUND);

        verify(requestActionRepository, times(1)).findById(requestActionId);
        verifyNoInteractions(fileAttachmentStorageService);
    }
}
