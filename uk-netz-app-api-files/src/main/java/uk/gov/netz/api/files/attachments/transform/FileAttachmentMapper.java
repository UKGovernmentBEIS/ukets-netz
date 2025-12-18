package uk.gov.netz.api.files.attachments.transform;

import org.mapstruct.Mapper;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.files.attachments.domain.FileAttachment;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileStatusInfoDTO;

import java.io.IOException;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface FileAttachmentMapper {

    FileAttachment toFileAttachment(FileDTO fileDTO) throws IOException;

    FileStatusInfoDTO toFileStatusInfoDTO(FileAttachment attachment);
}
