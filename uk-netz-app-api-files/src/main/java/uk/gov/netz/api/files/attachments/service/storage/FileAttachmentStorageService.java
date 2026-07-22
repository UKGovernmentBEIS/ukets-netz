package uk.gov.netz.api.files.attachments.service.storage;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.attachments.repository.FileAttachmentRepository;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.transform.FileMapper;
import uk.gov.netz.api.token.FileToken;
import uk.gov.netz.api.token.UserFileTokenService;

@Service
@RequiredArgsConstructor
public class FileAttachmentStorageService {

    private final FileAttachmentRepository fileAttachmentRepository;
    private final UserFileTokenService userFileTokenService;
    private static final FileMapper fileMapper = Mappers.getMapper(FileMapper.class);
    
    public FileToken generateGetFileAttachmentToken(String uuid) {
        if (!fileAttachmentRepository.existsByUuid(uuid)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, uuid);
        }

		return userFileTokenService.generateGetFileToken(uuid);
    }
    
    @Transactional(readOnly = true)
    public FileDTO getFileDTOByToken(String getFileUuidToken) {
        String fileUuid = userFileTokenService.resolveGetFileUuid(getFileUuidToken);
        return fileAttachmentRepository.findByUuid(fileUuid)
                .map(fileMapper::toFileDTO)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
