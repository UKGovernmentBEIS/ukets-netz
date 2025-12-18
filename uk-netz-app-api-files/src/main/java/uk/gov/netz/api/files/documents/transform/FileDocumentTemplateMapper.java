package uk.gov.netz.api.files.documents.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.files.common.domain.FileStatus;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.files.documents.domain.FileDocumentTemplate;

import java.util.UUID;

@Mapper(componentModel = "spring", config = MapperConfig.class, imports = UUID.class)
public interface FileDocumentTemplateMapper {

    FileDTO toFileDTO(FileDocumentTemplate fileDocumentTemplate);
    
    @Mapping(target = "name", source = "fileName")
    FileInfoDTO toFileInfoDTO(FileDocumentTemplate fileDocumentTemplate);
    
    @Mapping(target = "uuid", expression = "java(UUID.randomUUID().toString())")
    FileDocumentTemplate toFileDocumentTemplate(FileDTO fileDTO, FileStatus status, String createdBy);
}
