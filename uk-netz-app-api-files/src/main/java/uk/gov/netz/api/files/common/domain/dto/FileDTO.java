package uk.gov.netz.api.files.common.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileDTO {

    @NotBlank
    @Size(max = 255)
    private String fileName;
    
    @NotBlank
    private String fileType; // mime type
    
    @NotEmpty
    private byte[] fileContent;
    
    private long fileSize; // bytes

    @NotBlank
    private String createdBy;
}
