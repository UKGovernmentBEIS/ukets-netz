package uk.gov.netz.api.documenttemplate.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.gov.netz.api.files.common.domain.dto.FileDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTemplateFileInfoDTO {

    private boolean processRequired;

    private boolean convertRequired;

    @NotNull
    @Valid
    private FileDTO file;
}
