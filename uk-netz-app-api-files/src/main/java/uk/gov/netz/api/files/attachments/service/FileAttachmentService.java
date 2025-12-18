package uk.gov.netz.api.files.attachments.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.SetUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.attachments.domain.FileAttachment;
import uk.gov.netz.api.files.attachments.repository.FileAttachmentRepository;
import uk.gov.netz.api.files.attachments.transform.FileAttachmentMapper;
import uk.gov.netz.api.files.common.domain.FileStatus;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileStatusInfoDTO;
import uk.gov.netz.api.files.common.service.FileValidatorService;
import uk.gov.netz.api.files.common.transform.FileMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Validated
@RequiredArgsConstructor
public class FileAttachmentService {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final List<FileValidatorService> fileValidators;
    private static final FileAttachmentMapper fileAttachmentMapper = Mappers.getMapper(FileAttachmentMapper.class);
    private static final FileMapper fileMapper = Mappers.getMapper(FileMapper.class);

    @Transactional
    public String createFileAttachment(@Valid FileDTO fileDTO, FileStatus status,
                                       String createdBy) throws IOException {

        fileValidators.forEach(validator -> validator.validate(fileDTO));

        FileAttachment attachment = fileAttachmentMapper.toFileAttachment(fileDTO);
        attachment.setUuid(UUID.randomUUID().toString());
        attachment.setStatus(status);
        attachment.setCreatedBy(createdBy);

        fileAttachmentRepository.save(attachment);

        return attachment.getUuid();
    }

    @Transactional(readOnly = true)
    public FileDTO getFileDTO(String uuid) {
        return fileMapper.toFileDTO(findFileAttachmentByUuid(uuid));
    }
    
    @Transactional(readOnly = true)
    public List<FileDTO> getFiles(Set<String> uuids) {
        return fileAttachmentRepository.findAllByUuidIn(uuids).stream()
                .map(fileMapper::toFileDTO)
                .toList();
    }
    
    @Transactional(readOnly = true)
	public Stream<FileDTO> getFilesAsStream(Set<String> uuids) {
		return fileAttachmentRepository.streamAllByUuidIn(uuids).map(fileMapper::toFileDTO);
	}

    public List<FileStatusInfoDTO> getFilesStatus(Set<String> uuids) {
        return fileAttachmentRepository.findAllByUuidIn(uuids)
                .stream().map(fileAttachmentMapper::toFileStatusInfoDTO)
                .toList();
    }

    @Transactional
    public void updateFileAttachmentStatus(String uuid, FileStatus status) {
        FileAttachment fileAttachment = findFileAttachmentByUuid(uuid);
        fileAttachment.setStatus(status);
    }

    @Transactional
    public void updateFileAttachmentsStatus(Set<String> uuids, FileStatus status) {
        Set<String> persistedUuids = fileAttachmentRepository.findExistingUuidsByUuidIn(uuids);
        Set<String> notPersistedUuids = SetUtils.difference(uuids, persistedUuids);

        if(!notPersistedUuids.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, notPersistedUuids);
        }

        int totalCodes = uuids.size();
        int batchSize = 500;
        for (int i = 0; i < totalCodes; i += batchSize) {
            int end = Math.min(i + batchSize, totalCodes);
            List<String> batchUuids = new ArrayList<>(uuids).subList(i, end);
            fileAttachmentRepository.updateStatusInUuids(status, batchUuids);
        }
    }

    @Transactional
    public Set<String> deletePendingFileAttachments(Set<String> uuids) {
        Set<String> pendingUuids = fileAttachmentRepository.findExistingUuidsByUuidInAndStatus(uuids, FileStatus.PENDING);
        deleteFileAttachments(pendingUuids);

        return pendingUuids;
    }
    
    @Transactional
    public void deleteFileAttachments(Set<String> uuids) {
		int totalCodes = uuids.size();
		int batchSize = 500;
		for (int i = 0; i < totalCodes; i += batchSize) {
			int end = Math.min(i + batchSize, totalCodes);
			List<String> batchUuids = new ArrayList<>(uuids).subList(i, end);
			fileAttachmentRepository.deleteByUuidIn(batchUuids);
		}
    }

    public boolean fileAttachmentExist(String uuid) {
        return fileAttachmentsExist(Set.of(uuid));
    }

    public boolean fileAttachmentsExist(Set<String> uuids) {
    	Set<String> nonNullUuids =  uuids.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (nonNullUuids.isEmpty()) {
            return true;
        }
        return nonNullUuids.size() == fileAttachmentRepository.countAllByUuidIn(nonNullUuids);
    }

    private FileAttachment findFileAttachmentByUuid(String uuid) {
    	return fileAttachmentRepository.findByUuid(uuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, uuid));
    }
}
