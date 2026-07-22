package uk.gov.netz.api.documenttemplate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.DocumentTemplateAuthorityInfoProvider;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplate;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateDTO;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateInfoDTO;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateSearchCriteria;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateSearchResults;
import uk.gov.netz.api.documenttemplate.repository.DocumentTemplateRepository;
import uk.gov.netz.api.documenttemplate.transform.DocumentTemplateMapper;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.files.documents.service.FileDocumentTemplateService;

import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.netz.api.common.exception.ErrorCode.RESOURCE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DocumentTemplateQueryService implements DocumentTemplateAuthorityInfoProvider {

    private final DocumentTemplateRepository documentTemplateRepository;
    private final FileDocumentTemplateService fileDocumentTemplateService;
    private final DocumentTemplateMapper documentTemplateMapper;
    
    DocumentTemplate getDocumentTemplateById(Long id) {
        return documentTemplateRepository.findById(id)
            .orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND));
    }

    public DocumentTemplateSearchResults getDocumentTemplatesBySearchCriteria(DocumentTemplateSearchCriteria searchCriteria) {
        return documentTemplateRepository.findBySearchCriteria(searchCriteria);
    }

    @Transactional(readOnly = true)
    public DocumentTemplateDTO getDocumentTemplateDTOById(Long id) {
        final DocumentTemplate documentTemplate = getDocumentTemplateById(id);
        final FileInfoDTO fileInfoDTO = fileDocumentTemplateService.getFileInfoDocumentTemplateById(documentTemplate.getFileDocumentTemplateId());
        return documentTemplateMapper.toDocumentTemplateDTO(documentTemplate, fileInfoDTO);
    }
    
    @Transactional(readOnly = true)
    public Set<DocumentTemplateInfoDTO> getAllByNotificationTemplateId(Long notificationTemplateId){
		return documentTemplateRepository.findAllByNotificationTemplateId(notificationTemplateId).stream()
				.map(documentTemplateMapper::toDocumentTemplateInfoDTO).collect(Collectors.toSet());
    }

    @Override
    public CompetentAuthorityEnum getDocumentTemplateCaById(Long id) {
        return getDocumentTemplateById(id).getCompetentAuthority();
    }
}
