package uk.gov.netz.api.files.notes.transform;

import org.mapstruct.Mapper;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.notes.domain.FileNote;

import java.io.IOException;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface FileNoteMapper {

    FileNote toFileNote(FileDTO fileDTO) throws IOException;

}
