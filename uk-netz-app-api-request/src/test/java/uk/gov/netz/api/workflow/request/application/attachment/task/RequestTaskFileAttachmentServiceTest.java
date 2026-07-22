package uk.gov.netz.api.workflow.request.application.attachment.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.attachments.service.storage.FileAttachmentStorageService;
import uk.gov.netz.api.token.FileToken;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiResponseSubmitRequestTaskPayload;

@ExtendWith(MockitoExtension.class)
class RequestTaskFileAttachmentServiceTest {
	
	@InjectMocks
    private RequestTaskFileAttachmentService service;

	@Mock
    private RequestTaskService requestTaskService;

    @Mock
    private FileAttachmentStorageService fileAttachmentStorageService;

    @Test
    void shouldGenerateToken_whenAttachmentExists() {
        Long requestTaskId = 1L;
        UUID uuid = UUID.randomUUID();
        RequestTask requestTask = RequestTask.builder()
        		.payload(RfiResponseSubmitRequestTaskPayload.builder()
        				.rfiAttachments(Map.of(
        						uuid, "test1"
        						))
        				.build())
        		.build();
        
        FileToken expectedToken = new FileToken();

        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        when(fileAttachmentStorageService.generateGetFileAttachmentToken(uuid.toString()))
                .thenReturn(expectedToken);

        FileToken result = service.generateGetFileAttachmentToken(requestTaskId, uuid);

        // then
        assertEquals(expectedToken, result);
        verify(requestTaskService).findTaskById(requestTaskId);
        verify(fileAttachmentStorageService).generateGetFileAttachmentToken(uuid.toString());
    }

    @Test
    void shouldThrowException_whenAttachmentDoesNotExist() {
    	Long requestTaskId = 1L;
        UUID uuid = UUID.randomUUID();
        RequestTask requestTask = RequestTask.builder()
        		.payload(RfiResponseSubmitRequestTaskPayload.builder()
        				.rfiAttachments(Map.of(
        						UUID.randomUUID(), "test1"
        						))
        				.build())
        		.build();

        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);

        // when + then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.generateGetFileAttachmentToken(requestTaskId, uuid)
        );

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());

        verifyNoInteractions(fileAttachmentStorageService);
    }

}
