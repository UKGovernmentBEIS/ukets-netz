package uk.gov.netz.api.files.documents.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.FileType;
import uk.gov.netz.api.files.common.domain.FileStatus;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.files.common.service.FileValidatorService;
import uk.gov.netz.api.files.documents.domain.FileDocumentTemplate;
import uk.gov.netz.api.files.documents.repository.FileDocumentTemplateRepository;
import uk.gov.netz.api.files.documents.transform.FileDocumentTemplateMapper;

import java.util.List;

@Validated
@Service
@RequiredArgsConstructor
public class FileDocumentTemplateService {

    private final FileDocumentTemplateRepository fileDocumentTemplateRepository;
    private final List<FileValidatorService> fileValidators;
    private static final FileDocumentTemplateMapper fileDocumentTemplateMapper = Mappers.getMapper(FileDocumentTemplateMapper.class);

    public FileInfoDTO getFileInfoDocumentTemplateById(Long fileDocumentTemplateId) {
        return fileDocumentTemplateRepository.findById(fileDocumentTemplateId)
            .map(fileDocumentTemplateMapper::toFileInfoDTO)
            .orElseThrow(() ->  new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
    
    @Transactional(readOnly = true)
    public FileDTO getFileDocumentTemplateById(Long fileDocumentTemplateId) {
        return fileDocumentTemplateRepository.findById(fileDocumentTemplateId)
            .map(fileDocumentTemplateMapper::toFileDTO)
            .orElseThrow(() ->  new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional
    public Long createFileDocumentTemplate(@Valid FileDTO fileDTO, String authUserId) {
        validateFile(fileDTO);
        FileDocumentTemplate fileDocumentTemplate = fileDocumentTemplateMapper.toFileDocumentTemplate(fileDTO, FileStatus.SUBMITTED, authUserId);
        fileDocumentTemplateRepository.save(fileDocumentTemplate);
        return fileDocumentTemplate.getId();
    }
    
    @Transactional
    public void deleteFileDocumentTemplateById(Long fileDocumentTemplateId) {
        fileDocumentTemplateRepository.deleteById(fileDocumentTemplateId);
    }

    private void validateFile(FileDTO fileDTO) {
        if (!FileType.DOCX.getMimeTypes().contains(fileDTO.getFileType())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE, FileType.DOCX.getSimpleType());
        }
        fileValidators.forEach(validator -> validator.validate(fileDTO));
    }
}
