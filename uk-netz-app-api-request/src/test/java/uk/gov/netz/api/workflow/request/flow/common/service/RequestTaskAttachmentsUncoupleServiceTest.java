package uk.gov.netz.api.workflow.request.flow.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.files.attachments.service.FileAttachmentService;
import uk.gov.netz.api.files.common.domain.FileStatus;
import uk.gov.netz.api.files.common.domain.dto.FileStatusInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiQuestionPayload;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiResponsePayload;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiResponseSubmitRequestTaskPayload;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestTaskAttachmentsUncoupleServiceTest {

    @InjectMocks
    private RequestTaskAttachmentsUncoupleService service;

    @Mock
    private FileAttachmentService fileAttachmentService;

    @Mock
    private RequestTaskService requestTaskService;

    @Test
    void uncoupleAttachments() {
        UUID attachmentUuid = UUID.randomUUID();
        UUID unreferencedAttachmentUuid = UUID.randomUUID();
        Map<UUID, String> attachments = new HashMap<>();
        attachments.put(attachmentUuid, "file1");
        attachments.put(unreferencedAttachmentUuid, "unreference_file");
        RfiResponseSubmitRequestTaskPayload payload = RfiResponseSubmitRequestTaskPayload
            .builder()
            .rfiResponsePayload(RfiResponsePayload.builder().files(Set.of(attachmentUuid)).build())
            .rfiQuestionPayload(RfiQuestionPayload.builder().files(Set.of()).build())
            .rfiAttachments(attachments)
            .build();

        RequestTask requestTask = RequestTask.builder()
            .id(1L)
            .payload(payload)
            .build();

        when(requestTaskService.findTaskById(1L)).thenReturn(requestTask);

        service.uncoupleAttachments(1L);

        verify(fileAttachmentService, times(1)).updateFileAttachmentsStatus(Set.of(attachmentUuid.toString()), FileStatus.SUBMITTED);
        verify(fileAttachmentService, timeout(1000)).deletePendingFileAttachments(Set.of(unreferencedAttachmentUuid.toString()));

        assertThat(requestTask.getPayload()).isInstanceOf(RfiResponseSubmitRequestTaskPayload.class);
        RfiResponseSubmitRequestTaskPayload
            payloadSaved = (RfiResponseSubmitRequestTaskPayload) requestTask.getPayload();
        assertThat(payloadSaved.getAttachments()).containsOnlyKeys(attachmentUuid);
    }

    @Test
    void uncoupleAttachments_null_object_in_file_references() {
        Set<UUID> siteDiagrams = new HashSet<>();
        siteDiagrams.add(null);

        UUID unreferencedAttachmentUuid = UUID.randomUUID();
        Map<UUID, String> attachments = new HashMap<>();
        attachments.put(unreferencedAttachmentUuid, "unreference_file");
        RfiResponseSubmitRequestTaskPayload payload = RfiResponseSubmitRequestTaskPayload
            .builder()
            .rfiResponsePayload(RfiResponsePayload.builder().build())
            .rfiQuestionPayload(RfiQuestionPayload.builder().files(Set.of()).build())
            .rfiAttachments(attachments)
            .build();

        RequestTask requestTask = RequestTask.builder()
            .id(1L)
            .payload(payload)
            .build();

        when(requestTaskService.findTaskById(1L)).thenReturn(requestTask);

        service.uncoupleAttachments(1L);

        assertThat(requestTask.getPayload()).isInstanceOf(RfiResponseSubmitRequestTaskPayload.class);
        RfiResponseSubmitRequestTaskPayload
            payloadSaved = (RfiResponseSubmitRequestTaskPayload) requestTask.getPayload();
        assertThat(payloadSaved.getRfiAttachments()).isEmpty();


        verify(fileAttachmentService, timeout(1000)).deletePendingFileAttachments(Set.of(unreferencedAttachmentUuid.toString()));
        verify(fileAttachmentService, times(1)).updateFileAttachmentsStatus(Collections.emptySet(), FileStatus.SUBMITTED);
    }

    @Test
    void deletePendingAttachments() {

        final UUID attch1PersistedAndSubmitted = UUID.randomUUID();
        final UUID attch2PersistedAndPending = UUID.randomUUID();
        final UUID attch3Unpersisted = UUID.randomUUID();

        final Map<UUID, String> attachments = new HashMap<>();
        attachments.put(attch1PersistedAndSubmitted, "attch1PersistedAndSubmitted");
        attachments.put(attch2PersistedAndPending, "attch2PersistedAndPending");
        attachments.put(attch3Unpersisted, "attch3Unpersisted");

        final RfiResponseSubmitRequestTaskPayload payload =
        		RfiResponseSubmitRequestTaskPayload.builder()
        		.rfiResponsePayload(RfiResponsePayload.builder().files(Set.of(attch1PersistedAndSubmitted, attch2PersistedAndPending, attch3Unpersisted)).build())
        		.rfiQuestionPayload(RfiQuestionPayload.builder().files(Set.of()).build())
                .rfiAttachments(attachments)
                .build();

        RequestTask requestTask = RequestTask.builder()
            .id(1L)
            .payload(payload)
            .build();
        
        when(requestTaskService.findTaskById(1L)).thenReturn(requestTask);
        
        List<FileStatusInfoDTO> persistedAttachments = List.of(
        		FileStatusInfoDTO.builder().uuid(attch1PersistedAndSubmitted.toString()).status(FileStatus.SUBMITTED).build(),
        		FileStatusInfoDTO.builder().uuid(attch2PersistedAndPending.toString()).status(FileStatus.PENDING).build()
        		);
        
		when(fileAttachmentService.getFilesStatus(Set.of(attch1PersistedAndSubmitted.toString(),
				attch2PersistedAndPending.toString(), attch3Unpersisted.toString())))
			.thenReturn(persistedAttachments);
        
        service.deletePendingAttachments(1L);

        verify(fileAttachmentService, timeout(1000)).deleteFileAttachments(Set.of(attch2PersistedAndPending.toString()));

        assertThat(requestTask.getPayload()).isInstanceOf(RfiResponseSubmitRequestTaskPayload.class);
        RfiResponseSubmitRequestTaskPayload
            payloadSaved = (RfiResponseSubmitRequestTaskPayload) requestTask.getPayload();
        assertThat(payloadSaved.getAttachments()).containsOnlyKeys(attch1PersistedAndSubmitted);

    }
}
