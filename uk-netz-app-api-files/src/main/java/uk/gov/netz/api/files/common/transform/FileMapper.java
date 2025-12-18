package uk.gov.netz.api.files.common.transform;

import org.mapstruct.Mapper;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.files.common.domain.FileEntity;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface FileMapper {

    FileDTO toFileDTO(FileEntity fileEntity);
}
