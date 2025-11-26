package uk.gov.netz.api.workflow.request.application.item.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.workflow.request.core.domain.dto.UserInfoDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemAssigneeDTO {

    private UserInfoDTO taskAssignee;

    private String taskAssigneeType;
}
