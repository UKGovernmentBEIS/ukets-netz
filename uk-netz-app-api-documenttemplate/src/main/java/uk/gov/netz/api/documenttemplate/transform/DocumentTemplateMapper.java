package uk.gov.netz.api.documenttemplate.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplate;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateDTO;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateInfoDTO;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface DocumentTemplateMapper {

    @Mapping(target = "name", source = "documentTemplate.name")
    @Mapping(target = "fileUuid", source = "fileInfoDTO.uuid")
    @Mapping(target = "filename", source = "fileInfoDTO.name")
    DocumentTemplateDTO toDocumentTemplateDTO(DocumentTemplate documentTemplate, FileInfoDTO fileInfoDTO);
    
    @Mapping(target = "name", source = "documentTemplate.name")
    DocumentTemplateInfoDTO toDocumentTemplateInfoDTO(DocumentTemplate documentTemplate);
}
