package uk.gov.netz.api.workflow.request.application.taskview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestTaskItemDTO {

    private RequestTaskDTO requestTask;

    @Builder.Default
    private List<String> allowedRequestTaskActions = new ArrayList<>();

    private boolean userAssignCapable;

    private RequestInfoDTO requestInfo;
}
