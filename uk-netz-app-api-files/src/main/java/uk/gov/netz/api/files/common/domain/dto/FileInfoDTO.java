package uk.gov.netz.api.files.common.domain.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileInfoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
	private String name;
    private String uuid;
    
}
