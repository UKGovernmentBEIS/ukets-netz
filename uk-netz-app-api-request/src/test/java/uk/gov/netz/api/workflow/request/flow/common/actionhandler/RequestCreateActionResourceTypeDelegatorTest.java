package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.TestRequestCreateActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.repository.RequestTypeRepository;
import uk.gov.netz.api.workflow.request.core.validation.EnabledWorkflowValidator;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestCreateActionResourceTypeDelegatorTest {
    @Mock
    private EnabledWorkflowValidator enabledWorkflowValidator;

    @Mock
    private RequestTypeRepository requestTypeRepository;

    @Mock
    private RequestCreateActionResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionResourceTypeHandler;

    @BeforeEach
    void setUp() {
        requestCreateActionResourceTypeHandler = new RequestCreateActionResourceTypeHandler<>() {

            @Override
            public String process(String resourceId, String requestType, TestRequestCreateActionPayload payload, AppUser appUser) {
                return "requestId";
            }

            @Override
            public String getResourceType() {
                return "RESOURCE_TYPE1";
            }
        };
    }

    @Test
    void getResourceTypeHandler() {
        RequestType requestType1 = RequestType.builder()
                .code("requestTypeCode1")
                .resourceType("RESOURCE_TYPE1")
                .build();

        RequestType requestType2 = RequestType.builder()
                .code("requestTypeCode2")
                .resourceType("RESOURCE_TYPE2")
                .build();

        List<RequestCreateActionResourceTypeHandler<TestRequestCreateActionPayload>> handlers = List.of(requestCreateActionResourceTypeHandler);

        when(enabledWorkflowValidator.isWorkflowEnabled("requestTypeCode1")).thenReturn(true);
        when(requestTypeRepository.findAllByCanCreateManually(true)).thenReturn(Set.of(requestType1, requestType2));

        RequestCreateActionResourceTypeDelegator<TestRequestCreateActionPayload> requestCreateActionResourceTypeDelegator = new RequestCreateActionResourceTypeDelegator<>(handlers, enabledWorkflowValidator, requestTypeRepository);

        RequestCreateActionResourceTypeHandler<TestRequestCreateActionPayload> handler = requestCreateActionResourceTypeDelegator.getResourceTypeHandler("requestTypeCode1");

        assertThat(handler).isEqualTo(requestCreateActionResourceTypeHandler);
    }

    @Test
    void getResourceTypeHandler_cannot_create_manually() {
        RequestType requestType2 = RequestType.builder()
                .code("requestTypeCode2")
                .resourceType("RESOURCE_TYPE2")
                .build();

        List<RequestCreateActionResourceTypeHandler<TestRequestCreateActionPayload>> handlers = List.of(requestCreateActionResourceTypeHandler);

        when(requestTypeRepository.findAllByCanCreateManually(true)).thenReturn(Set.of(requestType2));

        RequestCreateActionResourceTypeDelegator<TestRequestCreateActionPayload> requestCreateActionResourceTypeDelegator = new RequestCreateActionResourceTypeDelegator<>(handlers, enabledWorkflowValidator, requestTypeRepository);

        BusinessException businessException = assertThrows(BusinessException.class, () -> requestCreateActionResourceTypeDelegator.getResourceTypeHandler("requestTypeCode1"));

        assertEquals(ErrorCode.REQUEST_CREATE_ACTION_NOT_ALLOWED, businessException.getErrorCode());
    }

    @Test
    void getResourceTypeHandler_workflow_not_enabled() {
        RequestType requestType2 = RequestType.builder()
                .code("requestTypeCode2")
                .resourceType("RESOURCE_TYPE2")
                .build();

        List<RequestCreateActionResourceTypeHandler<TestRequestCreateActionPayload>> handlers = List.of(requestCreateActionResourceTypeHandler);

        when(requestTypeRepository.findAllByCanCreateManually(true)).thenReturn(Set.of(requestType2));

        RequestCreateActionResourceTypeDelegator<TestRequestCreateActionPayload> requestCreateActionResourceTypeDelegator = new RequestCreateActionResourceTypeDelegator<>(handlers, enabledWorkflowValidator, requestTypeRepository);

        BusinessException businessException = assertThrows(BusinessException.class, () -> requestCreateActionResourceTypeDelegator.getResourceTypeHandler("requestTypeCode1"));

        assertEquals(ErrorCode.REQUEST_CREATE_ACTION_NOT_ALLOWED, businessException.getErrorCode());
        assertTrue(Arrays.asList(businessException.getData()).contains("requestTypeCode1 is not supported"));
    }

    @Test
    void getResourceTypeHandler_handler_not_found() {
        RequestType requestType1 = RequestType.builder()
                .code("requestTypeCode1")
                .resourceType("RESOURCE_TYPE")
                .build();

        List<RequestCreateActionResourceTypeHandler<TestRequestCreateActionPayload>> handlers = List.of(requestCreateActionResourceTypeHandler);

        when(requestTypeRepository.findAllByCanCreateManually(true)).thenReturn(Set.of(requestType1));

        RequestCreateActionResourceTypeDelegator<TestRequestCreateActionPayload> requestCreateActionResourceTypeDelegator = new RequestCreateActionResourceTypeDelegator<>(handlers, enabledWorkflowValidator, requestTypeRepository);

        BusinessException businessException = assertThrows(BusinessException.class, () -> requestCreateActionResourceTypeDelegator.getResourceTypeHandler("requestTypeCode1"));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, businessException.getErrorCode());
        assertTrue(Arrays.asList(businessException.getData()).contains("RESOURCE_TYPE"));
    }
}