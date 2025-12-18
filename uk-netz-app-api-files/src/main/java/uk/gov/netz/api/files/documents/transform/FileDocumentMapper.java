package uk.gov.netz.api.files.documents.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.files.documents.domain.FileDocument;

import java.util.UUID;

@Mapper(componentModel = "spring", config = MapperConfig.class, imports = UUID.class)
public interface FileDocumentMapper {

    FileDTO toFileDTO(FileDocument fileDocument);
    
    @Mapping(target = "name", source = "fileName")
    FileInfoDTO toFileInfoDTO(FileDocument fileDocument);
}
