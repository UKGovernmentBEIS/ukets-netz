package uk.gov.netz.api.workflow.request.common.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;

import io.hypersistence.utils.hibernate.type.util.ObjectMapperWrapper;
import jakarta.persistence.EntityManager;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRule;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.bpmn.WorkflowEngineType;
import uk.gov.netz.api.workflow.request.application.item.domain.RequestTaskVisit;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestAction;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionType;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.enumeration.SupportingTaskType;
import uk.gov.netz.api.workflow.request.flow.common.jsonprovider.RequestActionPayloadCommonTypesProvider;
import uk.gov.netz.api.workflow.request.flow.common.jsonprovider.RequestCreateActionPayloadCommonTypesProvider;
import uk.gov.netz.api.workflow.request.flow.common.jsonprovider.RequestTaskActionPayloadCommonTypesProvider;
import uk.gov.netz.api.workflow.request.flow.common.jsonprovider.RequestTaskPayloadCommonTypesProvider;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentCancelledRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentStatus;

public abstract class RequestAbstractTest extends AbstractContainerBaseTest {

	@Autowired
    protected EntityManager entityManager;
	
	@Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    public void setUp() {
    	objectMapper.registerSubtypes(new RequestActionPayloadCommonTypesProvider().getTypes().toArray(NamedType[]::new));
    	objectMapper.registerSubtypes(new RequestCreateActionPayloadCommonTypesProvider().getTypes().toArray(NamedType[]::new));
    	objectMapper.registerSubtypes(new RequestTaskActionPayloadCommonTypesProvider().getTypes().toArray(NamedType[]::new));
    	objectMapper.registerSubtypes(new RequestTaskPayloadCommonTypesProvider().getTypes().toArray(NamedType[]::new));
    	ObjectMapperWrapper.INSTANCE.setObjectMapper(objectMapper);
    }
	
	protected RequestType createRequestType(String code, String description, String processDefinitionId,
			String historyCategory, boolean holdHistory, boolean displayedInProgress, boolean cascadable,
			boolean canCreateManually, String resourceType) {
		RequestType requestType = 
				RequestType.builder()
                    .code(code)
                    .description(description)
                    .processDefinitionId(processDefinitionId)
                    .historyCategory(historyCategory)
                    .holdHistory(holdHistory)
                    .displayedInProgress(displayedInProgress)
                    .cascadable(cascadable)
                    .canCreateManually(canCreateManually)
                    .resourceType(resourceType)
                    .build();
        entityManager.persist(requestType);
        return requestType;
    }
	
	protected Request createRequest(Long accountId, CompetentAuthorityEnum ca, Long vbId, 
			RequestType requestType, String processInstanceId, String status, LocalDateTime creationDate) {
		return createRequest(accountId, ca, vbId, requestType, processInstanceId, status, creationDate, WorkflowEngineType.CAMUNDA);
	}

	protected Request createRequest(Long accountId, CompetentAuthorityEnum ca, Long vbId, 
			RequestType requestType, String processInstanceId, String status, LocalDateTime creationDate, WorkflowEngineType engine) {
        Request request = Request.builder()
                .id(RandomStringUtils.insecure().next(5))
                .type(requestType)
                .engine(engine)
                .processInstanceId(processInstanceId)
                .status(status)
                .creationDate(creationDate)
                .build();
        addResourcesToRequest(accountId, ca, vbId, request);

        entityManager.persist(request);
        return request;
    }
    
	protected RequestTaskType createRequestTaskType(String code, RequestType requestType, boolean assignable, String expirationKey,
			boolean canCreateManually, boolean supporting) {
		RequestTaskType requestTaskType = 
				RequestTaskType.builder()
                    .code(code)
                    .requestType(requestType)
                    .assignable(assignable)
                    .expirationKey(expirationKey)
                    .supporting(SupportingTaskType.DEFAULT)
                    .build();
        entityManager.persist(requestTaskType);
        return requestTaskType;
    }

	protected RequestTask createRequestTask(String assignee, Request request, RequestTaskType taskType,
                                          String processTaskId, LocalDateTime startDate) {
        RequestTask requestTask = RequestTask.builder()
                .request(request)
                .processTaskId(processTaskId)
                .type(taskType)
                .assignee(assignee)
                .startDate(LocalDateTime.now())
                .dueDate(LocalDate.now().plusMonths(1L))
                .build();

        entityManager.persist(requestTask);
        requestTask.setStartDate(startDate);

        return requestTask;
    }
	
	protected RequestAction createRequestAction(Request request, String type, String submitterId, String submitter, LocalDateTime creationDate) {
		RequestAction requestAction = RequestAction.builder()
            .request(request)
            .type(type)
            .payload(PaymentCancelledRequestActionPayload.builder().payloadType(RequestActionPayloadTypes.PAYMENT_CANCELLED_PAYLOAD).status(PaymentStatus.CANCELLED).cancellationReason("ff").build())
            .submitterId(submitterId)
            .submitter(submitter)
            .creationDate(creationDate).build();
		entityManager.persist(requestAction);
		return requestAction;
	}
	
	protected void createOpenedItem(Long taskId, String userId) {
        RequestTaskVisit requestTaskVisit = RequestTaskVisit.builder()
                .taskId(taskId)
                .userId(userId).build();

        entityManager.persist(requestTaskVisit);
    }
	
	protected RequestTaskActionType createRequestTaskActionType(String code, boolean blockedByPayment) {
		RequestTaskActionType requestTaskActionType = 
				RequestTaskActionType.builder()
                    .code(code)
                    .blockedByPayment(blockedByPayment)
                    .build();
        entityManager.persist(requestTaskActionType);
        return requestTaskActionType;
    }
	
	protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
	
	protected void addResourcesToRequest(Long accountId, CompetentAuthorityEnum competentAuthority, Long vbId, Request request) {
		RequestResource caResource = RequestResource.builder()
				.resourceType(ResourceType.CA)
				.resourceId(competentAuthority.name())
				.request(request)
				.build();
		
		request.getRequestResources().add(caResource);
		
		if(accountId != null) {
			RequestResource accountResource = RequestResource.builder()
					.resourceType(ResourceType.ACCOUNT)
					.resourceId(accountId.toString())
					.request(request)
					.build();
			
			request.getRequestResources().add(accountResource);	
		}
		
		if (vbId != null) {
			RequestResource vbIdResource = RequestResource.builder()
					.resourceType(ResourceType.VERIFICATION_BODY)
					.resourceId(vbId.toString())
					.request(request)
					.build();
			request.getRequestResources().add(vbIdResource);
		}
	}
	
	protected AuthorizationRule createAuthorizationRuleForRequest(String resourceType, String resourceSubType, String handler, String scope, String roleType) {
		AuthorizationRule rule = AuthorizationRule.builder()
                .resourceType(resourceType)
                .resourceSubType(resourceSubType)
                .handler(handler)
                .scope(scope)
                .roleType(roleType)
                .build();
		entityManager.persist(rule);
		return rule;
	}
}
