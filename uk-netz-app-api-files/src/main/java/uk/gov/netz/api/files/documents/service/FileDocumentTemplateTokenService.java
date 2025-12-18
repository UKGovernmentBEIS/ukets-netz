package uk.gov.netz.api.files.documents.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.transform.FileMapper;
import uk.gov.netz.api.files.documents.repository.FileDocumentTemplateRepository;
import uk.gov.netz.api.token.FileToken;
import uk.gov.netz.api.token.UserFileTokenService;

@Service
@RequiredArgsConstructor
public class FileDocumentTemplateTokenService {

    private final FileDocumentTemplateRepository fileDocumentTemplateRepository;
    private final UserFileTokenService userFileTokenService;
    private static final FileMapper fileMapper = Mappers.getMapper(FileMapper.class);

    public FileToken generateGetFileDocumentTemplateToken(String fileUuid) {
        if (!fileDocumentTemplateRepository.existsByUuid(fileUuid)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, fileUuid);
        }

        return userFileTokenService.generateGetFileToken(fileUuid);
    }

    @Transactional(readOnly = true)
    public FileDTO getFileDTOByToken(String getFileToken) {
        String fileUuid = userFileTokenService.resolveGetFileUuid(getFileToken);
        return fileDocumentTemplateRepository.findByUuid(fileUuid)
                .map(fileMapper::toFileDTO)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
