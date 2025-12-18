package uk.gov.netz.api.files.attachments.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.files.attachments.domain.FileAttachment;
import uk.gov.netz.api.files.attachments.repository.FileAttachmentRepository;
import uk.gov.netz.api.files.common.domain.FileStatus;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.service.FileScanValidatorService;
import uk.gov.netz.api.files.common.service.FileValidatorService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.common.exception.ErrorCode.RESOURCE_NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class FileAttachmentServiceTest {

    @InjectMocks
    private FileAttachmentService service;
    
    @Mock
    private FileAttachmentRepository fileAttachmentRepository;

    @Mock
    private FileScanValidatorService fileScanValidator;

    @Spy
    private ArrayList<FileValidatorService> fileValidators;

    @BeforeEach
    void setUp() {
        fileValidators.add(fileScanValidator);
    }

    @Test
    void createFileAttachment() throws IOException {
        byte[] contentBytes = "dummycontent".getBytes();
        FileDTO fileDTO = FileDTO.builder()
                .fileName("name")
                .fileSize(contentBytes.length)
                .fileType("application/pdf")
                .fileContent(contentBytes)
                .build();
        FileStatus status = FileStatus.PENDING;
        
        String attachmentUuid = service.createFileAttachment(fileDTO, status, "user");
        
        assertThat(attachmentUuid).isNotNull();
        ArgumentCaptor<FileAttachment> attachmentCaptor = ArgumentCaptor.forClass(FileAttachment.class);
        verify(fileAttachmentRepository, times(1)).save(attachmentCaptor.capture());
        FileAttachment attachmentCaptured = attachmentCaptor.getValue();
        assertThat(attachmentCaptured.getFileName()).isEqualTo(fileDTO.getFileName());
        assertThat(attachmentCaptured.getFileSize()).isEqualTo(fileDTO.getFileSize());
        assertThat(attachmentCaptured.getFileType()).isEqualTo(fileDTO.getFileType());
        assertThat(attachmentCaptured.getFileContent()).isEqualTo(contentBytes);
        assertThat(attachmentCaptured.getCreatedBy()).isEqualTo("user");
        assertThat(attachmentCaptured.getStatus()).isEqualTo(status);
        assertThat(attachmentCaptured.getUuid()).isEqualTo(attachmentUuid);

        verify(fileScanValidator, times(1)).validate(fileDTO);
    }

    @Test
    void getFileDTO() {
        String uuid = "uuid";
        FileAttachment fileAttachment = FileAttachment.builder()
            .fileName("name")
            .fileSize(121210)
            .fileType("application/pdf")
            .fileContent(new byte[]{})
            .build();

        when(fileAttachmentRepository.findByUuid(uuid)).thenReturn(Optional.of(fileAttachment));
        FileDTO fileDTO = service.getFileDTO("uuid");

        assertThat(fileDTO.getFileName()).isEqualTo(fileAttachment.getFileName());
        assertThat(fileDTO.getFileType()).isEqualTo(fileAttachment.getFileType());
        assertThat(fileDTO.getFileContent()).isEqualTo(fileAttachment.getFileContent());
    }

    @Test
    void getFiles() {
        String uuid = "uuid";
        FileAttachment fileAttachment = FileAttachment.builder()
                .fileName("name")
                .fileSize(121210)
                .fileType("application/pdf")
                .fileContent(new byte[]{})
                .build();

        when(fileAttachmentRepository.findAllByUuidIn(Set.of(uuid)))
                .thenReturn(List.of(fileAttachment));
        List<FileDTO> files = service.getFiles(Set.of(uuid));

        assertThat(files).hasSize(1);
        assertThat(files.getFirst().getFileName()).isEqualTo(fileAttachment.getFileName());
        assertThat(files.getFirst().getFileType()).isEqualTo(fileAttachment.getFileType());
        assertThat(files.getFirst().getFileContent()).isEqualTo(fileAttachment.getFileContent());
    }

    @Test
    void updateFileAttachmentStatus() {
        String uuid = "uuid";
        FileAttachment fileAttachment = FileAttachment.builder()
            .fileName("name")
            .fileSize(121210)
            .fileType("application/pdf")
            .fileContent(new byte[]{})
            .status(FileStatus.PENDING)
            .build();

        when(fileAttachmentRepository.findByUuid(uuid)).thenReturn(Optional.of(fileAttachment));
        service.updateFileAttachmentStatus("uuid", FileStatus.SUBMITTED);

        assertThat(fileAttachment.getStatus()).isEqualTo(FileStatus.SUBMITTED);
    }

    @Test
    void updateFileAttachmentsStatus() {
        final FileStatus status = FileStatus.SUBMITTED;
        final Set<String> uuidsBatch1 = IntStream.range(1, 501)
                .mapToObj(i -> "uuid" + i).collect(Collectors.toCollection(LinkedHashSet::new));
        final Set<String> uuidsBatch2 = IntStream.range(501, 1000)
                .mapToObj(i -> "uuid" + i).collect(Collectors.toCollection(LinkedHashSet::new));
        final Set<String> uuids = Stream.of(uuidsBatch1, uuidsBatch2)
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        when(fileAttachmentRepository.findExistingUuidsByUuidIn(uuids)).thenReturn(uuids);

        service.updateFileAttachmentsStatus(uuids, status);

        verify(fileAttachmentRepository, times(1))
                .findExistingUuidsByUuidIn(uuids);
        verify(fileAttachmentRepository, times(1))
                .updateStatusInUuids(status, new ArrayList<>(uuidsBatch1));
        verify(fileAttachmentRepository, times(1))
                .updateStatusInUuids(status, new ArrayList<>(uuidsBatch2));
        verifyNoMoreInteractions(fileAttachmentRepository);
    }

    @Test
    void updateFileAttachmentsStatus_resource_not_found() {
        final FileStatus status = FileStatus.SUBMITTED;
        final Set<String> uuids = Set.of("uuid1", "uuid2");

        when(fileAttachmentRepository.findExistingUuidsByUuidIn(uuids)).thenReturn(Set.of("uuid1"));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                service.updateFileAttachmentsStatus(uuids, status));

        assertThat(ex.getErrorCode()).isEqualTo(RESOURCE_NOT_FOUND);
        assertThat(ex.getData()).hasSize(1).containsExactly(Set.of("uuid2"));
        verify(fileAttachmentRepository, times(1))
                .findExistingUuidsByUuidIn(uuids);
        verifyNoMoreInteractions(fileAttachmentRepository);
    }

    @Test
    void deletePendingFileAttachments() {
        final Set<String> uuidsBatch1 = IntStream.range(1, 501)
                .mapToObj(i -> "uuid" + i).collect(Collectors.toCollection(LinkedHashSet::new));
        final Set<String> uuidsBatch2 = IntStream.range(501, 1000)
                .mapToObj(i -> "uuid" + i).collect(Collectors.toCollection(LinkedHashSet::new));
        final Set<String> uuids = Stream.of(uuidsBatch1, uuidsBatch2)
                .flatMap(Set::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        when(fileAttachmentRepository.findExistingUuidsByUuidInAndStatus(uuids, FileStatus.PENDING)).thenReturn(uuids);

        Set<String> result = service.deletePendingFileAttachments(uuids);

        assertThat(result).isEqualTo(uuids);
        verify(fileAttachmentRepository, times(1))
                .findExistingUuidsByUuidInAndStatus(uuids, FileStatus.PENDING);
        verify(fileAttachmentRepository, times(1))
                .deleteByUuidIn(new ArrayList<>(uuidsBatch1));
        verify(fileAttachmentRepository, times(1))
                .deleteByUuidIn(new ArrayList<>(uuidsBatch2));
        verifyNoMoreInteractions(fileAttachmentRepository);
    }
    
    @Test
    void fileAttachmentsExist() {
        Set<String> uuids = new HashSet<>(Arrays.asList("uuid1", "uuid2", null));

        when(fileAttachmentRepository.countAllByUuidIn(Set.of("uuid1", "uuid2"))).thenReturn(2L);
        boolean deleted = service.fileAttachmentsExist(uuids);

        assertThat(deleted).isTrue();

        verify(fileAttachmentRepository, times(1)).countAllByUuidIn(Set.of("uuid1", "uuid2"));
    }
    
}
