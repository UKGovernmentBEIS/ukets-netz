package uk.gov.netz.api.workflow.request.core.domain.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestTaskPreviewFileInfoDTO {

	private String asyncJobId;
	
	private RequestTaskPreviewFileStatus status;
	
	private FileInfoDTO file;
	
	private LocalDateTime createdDate;
	
}
