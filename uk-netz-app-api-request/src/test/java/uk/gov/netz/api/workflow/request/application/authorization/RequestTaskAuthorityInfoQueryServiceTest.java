package uk.gov.netz.api.workflow.request.application.authorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.RequestTaskAuthorityInfoDTO;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.ResourceAuthorityInfo;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.competentauthority.CompetentAuthorityEnum.ENGLAND;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class RequestTaskAuthorityInfoQueryServiceTest {

    private static final String REQUEST_ID = "1";
    private static final Long REQUEST_TASK_ID = 1L;
    private static final String PROCESS_INSTANCE_ID = "process_instance_id";
    private static final String PROCESS_TASK_ID = "process_task_id";
    private static final Long ACCOUNT_ID = 1L;

    @InjectMocks
    private RequestTaskAuthorityInfoQueryService service;

    @Mock
    private RequestTaskService requestTaskService;

    @Test
    void getRequestTaskInfo() {
        Request request = createRequest("requestTypeCode1");
        RequestTask requestTask = createRequestTask(request, PROCESS_TASK_ID, "requestTaskTypeCode1", "assignee");

        when(requestTaskService.findTaskById(REQUEST_TASK_ID)).thenReturn(requestTask);

        RequestTaskAuthorityInfoDTO requestTaskInfoDTO = service.getRequestTaskInfo(REQUEST_TASK_ID);

        RequestTaskAuthorityInfoDTO expectedRequestTaskInfoDTO = RequestTaskAuthorityInfoDTO.builder()
                .type(requestTask.getType().getCode())
                .authorityInfo(ResourceAuthorityInfo.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, ACCOUNT_ID.toString(), 
                				ResourceType.CA, ENGLAND.name()))
                        .build())
                .assignee(requestTask.getAssignee())
                .requestType(request.getType().getCode())
                .build();
        assertEquals(expectedRequestTaskInfoDTO, requestTaskInfoDTO);
    }

    @Test
    void getRequestTaskInfo_does_not_exist() {
        when(requestTaskService.findTaskById(REQUEST_TASK_ID)).thenThrow(new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        BusinessException businessException = assertThrows(BusinessException.class,
                () -> service.getRequestTaskInfo(REQUEST_TASK_ID));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, businessException.getErrorCode());
    }

    private Request createRequest(String requestTypeCode) {
        Request request = new Request();
        request.setId(REQUEST_ID);
        request.setProcessInstanceId(PROCESS_INSTANCE_ID);
        request.setType(RequestType.builder().code(requestTypeCode).build());
        addResourcesToRequest(ACCOUNT_ID, ENGLAND, request);
        return request;
    }

    private RequestTask createRequestTask(Request request, String processTaskId, String taskTypeCode, String assignee) {
        return RequestTask.builder()
            .request(request)
            .processTaskId(processTaskId)
            .type(RequestTaskType.builder().code(taskTypeCode).build())
            .assignee(assignee)
            .build();
    }
    
    private void addResourcesToRequest(Long accountId, CompetentAuthorityEnum competentAuthority, Request request) {
		RequestResource caResource = RequestResource.builder()
				.resourceType(ResourceType.CA)
				.resourceId(competentAuthority.name())
				.request(request)
				.build();
		
		RequestResource accountResource = RequestResource.builder()
				.resourceType(ResourceType.ACCOUNT)
				.resourceId(accountId.toString())
				.request(request)
				.build();

        request.getRequestResources().addAll(List.of(caResource, accountResource));
	}
}
