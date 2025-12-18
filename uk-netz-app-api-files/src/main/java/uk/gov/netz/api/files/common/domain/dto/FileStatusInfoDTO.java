package uk.gov.netz.api.files.common.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.files.common.domain.FileStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileStatusInfoDTO {
    private String uuid;
    private FileStatus status;
}
