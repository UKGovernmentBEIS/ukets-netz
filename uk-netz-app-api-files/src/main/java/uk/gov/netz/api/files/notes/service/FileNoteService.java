package uk.gov.netz.api.files.notes.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.common.utils.DateService;
import uk.gov.netz.api.files.common.domain.FileStatus;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.files.common.domain.dto.FileUuidDTO;
import uk.gov.netz.api.files.common.service.FileValidatorService;
import uk.gov.netz.api.files.notes.domain.FileNote;
import uk.gov.netz.api.files.notes.repository.FileNoteRepository;
import uk.gov.netz.api.files.notes.transform.FileNoteMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class FileNoteService {
    
    private static final int DAYS_CUTOFF = 1;
    
    private final DateService dateService;
    private final FileNoteRepository fileNoteRepository;
    private final List<FileValidatorService> fileValidators;
    private final FileNoteMapper fileNoteMapper;

    public void cleanUpUnusedFiles() {
        
        final LocalDateTime expirationDate = dateService.getLocalDateTime().minusDays(DAYS_CUTOFF);
        fileNoteRepository.deleteFilesByStatusAndLastUpdatedDateBefore(FileStatus.PENDING, expirationDate);
    }

    public Map<UUID, String> getFileNames(final Set<UUID> uuids) {
        
        return fileNoteRepository.getFileNamesByUuid(uuidsToStrings(uuids)).stream().collect(
            Collectors.toMap(
                f -> UUID.fromString(f.getUuid()), 
                FileInfoDTO::getName
            ));
    }

    public FileUuidDTO uploadAccountFile(String userId, FileDTO fileDTO, Long accountId) throws IOException {

        final FileNote fileNote = this.validateAndCreateFileNote(userId, fileDTO);
        fileNote.setAccountId(accountId);
        fileNoteRepository.save(fileNote);

        return FileUuidDTO.builder().uuid(fileNote.getUuid()).build();
    }
    
    public FileUuidDTO uploadRequestFile(final String userId, final FileDTO fileDTO, final String requestId) throws IOException {

        final FileNote fileNote = this.validateAndCreateFileNote(userId, fileDTO);
        fileNote.setRequestId(requestId);
        fileNoteRepository.save(fileNote);

        return FileUuidDTO.builder().uuid(fileNote.getUuid()).build();
    }
    
    public void submitFiles(final Set<UUID> uuids) {
        fileNoteRepository.updateNoteFilesStatusByUuid(uuidsToStrings(uuids), FileStatus.SUBMITTED);
    }
    
    public void deleteFiles(final Set<UUID> uuids) {
        fileNoteRepository.deleteNoteFilesByUuid(uuidsToStrings(uuids));
    }

    private FileNote validateAndCreateFileNote(final String userId, final FileDTO fileDTO) throws IOException {

        fileValidators.forEach(validator -> validator.validate(fileDTO));

        final FileNote fileNote = fileNoteMapper.toFileNote(fileDTO);
        final String uuid = UUID.randomUUID().toString();
        fileNote.setUuid(uuid);
        fileNote.setStatus(FileStatus.PENDING);
        fileNote.setCreatedBy(userId);
        
        return fileNote;
    }
    
    private static Set<String> uuidsToStrings(final Set<UUID> uuids) {
    	return uuids.stream().filter(Objects::nonNull).map(UUID::toString).collect(Collectors.toSet());
    }
}
