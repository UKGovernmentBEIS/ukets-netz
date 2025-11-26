package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.TestRequestCreateActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestCreateActionPayload;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestCreateByAccountValidator;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestCreateByRequestValidator;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestCreateActionAccountResourceTypeHandlerTest {
    @Mock
    private AccountQueryService accountQueryService;

    @Mock
    private RequestCreateByAccountValidator requestCreateByAccountValidator;

    @Mock
    private RequestCreateByRequestValidator requestCreateByRequestValidator;

    @Mock
    private RequestAccountCreateActionHandler<TestRequestCreateActionPayload> requestAccountCreateActionHandler;

    @Test
    void process() {
        requestCreateByAccountValidator = new RequestCreateByAccountValidator() {

            @Override
            public String getRequestType() {
                return "requestType1";
            }

            @Override
            public RequestCreateValidationResult validateAction(Long accountId) {
                return RequestCreateValidationResult.builder()
                        .valid(true)
                        .build();
            }
        };

        requestAccountCreateActionHandler = new RequestAccountCreateActionHandler<>() {

            @Override
            public String process(Long accountId, TestRequestCreateActionPayload payload, AppUser appUser) {
                return "requestId";
            }

            @Override
            public String getRequestType() {
                return "requestType1";
            }
        };

        TestRequestCreateActionPayload testRequestCreateActionPayload = TestRequestCreateActionPayload.builder()
                .payloadType("PAYLOAD_TYPE")
                .build();

        AppUser appUser = AppUser.builder().build();

        List<RequestAccountCreateActionHandler<TestRequestCreateActionPayload>> handlers = List.of(requestAccountCreateActionHandler);
        List<RequestCreateByAccountValidator> createByAccountValidators = List.of(requestCreateByAccountValidator);
        List<RequestCreateByRequestValidator<TestRequestCreateActionPayload>> createByRequestValidators = List.of();

        RequestCreateActionAccountResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionResourceTypeHandler =
                new RequestCreateActionAccountResourceTypeHandler<>(createByAccountValidators,createByRequestValidators, handlers, accountQueryService);
        String requestId = requestCreateActionResourceTypeHandler.process("1", "requestType1", testRequestCreateActionPayload, appUser);

        assertEquals(requestId, "requestId");

        verify(accountQueryService, times(1)).exclusiveLockAccount(1L);
    }

    @Test
    void process_validator_not_found() {
        requestCreateByAccountValidator = new RequestCreateByAccountValidator() {

            @Override
            public String getRequestType() {
                return "requestType2";
            }

            @Override
            public RequestCreateValidationResult validateAction(Long accountId) {
                return RequestCreateValidationResult.builder()
                        .valid(true)
                        .build();
            }
        };

        requestAccountCreateActionHandler = new RequestAccountCreateActionHandler<>() {

            @Override
            public String process(Long accountId, TestRequestCreateActionPayload payload, AppUser appUser) {
                return "requestId";
            }

            @Override
            public String getRequestType() {
                return "requestType1";
            }
        };

        TestRequestCreateActionPayload testRequestCreateActionPayload = TestRequestCreateActionPayload.builder()
                .payloadType("PAYLOAD_TYPE")
                .build();

        AppUser appUser = AppUser.builder().build();

        List<RequestAccountCreateActionHandler<TestRequestCreateActionPayload>> handlers = List.of(requestAccountCreateActionHandler);
        List<RequestCreateByAccountValidator> createByAccountValidators = List.of(requestCreateByAccountValidator);
        List<RequestCreateByRequestValidator<TestRequestCreateActionPayload>> createByRequestValidators = List.of();

        RequestCreateActionAccountResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionResourceTypeHandler = new RequestCreateActionAccountResourceTypeHandler<>(createByAccountValidators, createByRequestValidators, handlers, accountQueryService);
        String requestId = requestCreateActionResourceTypeHandler.process("1", "requestType1", testRequestCreateActionPayload, appUser);

        assertEquals(requestId, "requestId");

        verify(accountQueryService, times(1)).exclusiveLockAccount(1L);
    }

    @Test
    void process_validator_invalid() {
        requestCreateByAccountValidator = new RequestCreateByAccountValidator() {

            @Override
            public String getRequestType() {
                return "requestType1";
            }

            @Override
            public RequestCreateValidationResult validateAction(Long accountId) {
                return RequestCreateValidationResult.builder()
                        .valid(false)
                        .build();
            }
        };

        TestRequestCreateActionPayload testRequestCreateActionPayload = TestRequestCreateActionPayload.builder()
                .payloadType("PAYLOAD_TYPE")
                .build();

        AppUser appUser = AppUser.builder().build();

        List<RequestAccountCreateActionHandler<TestRequestCreateActionPayload>> handlers = List.of();
        List<RequestCreateByAccountValidator> createByAccountValidators = List.of(requestCreateByAccountValidator);
        List<RequestCreateByRequestValidator<TestRequestCreateActionPayload>> createByRequestValidators = List.of();

        RequestCreateActionAccountResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionResourceTypeHandler = new RequestCreateActionAccountResourceTypeHandler<>(createByAccountValidators, createByRequestValidators, handlers, accountQueryService);

        BusinessException businessException = assertThrows(BusinessException.class, () -> requestCreateActionResourceTypeHandler.process("1", "requestType1", testRequestCreateActionPayload, appUser));

        assertEquals(ErrorCode.REQUEST_CREATE_ACTION_NOT_ALLOWED, businessException.getErrorCode());
        assertTrue(Arrays.asList(businessException.getData()).contains(RequestCreateValidationResult.builder()
                .valid(false)
                .build()));
        verify(accountQueryService, times(1)).exclusiveLockAccount(1L);
    }

    @Test
    void process_validator_not_available() {
        requestCreateByAccountValidator = new RequestCreateByAccountValidator() {

            @Override
            public String getRequestType() {
                return "requestType1";
            }

            @Override
            public RequestCreateValidationResult validateAction(Long accountId) {
                return RequestCreateValidationResult.builder()
                        .valid(true)
                        .isAvailable(false)
                        .build();
            }
        };

        TestRequestCreateActionPayload testRequestCreateActionPayload = TestRequestCreateActionPayload.builder()
                .payloadType("PAYLOAD_TYPE")
                .build();

        AppUser appUser = AppUser.builder().build();

        List<RequestAccountCreateActionHandler<TestRequestCreateActionPayload>> handlers = List.of();
        List<RequestCreateByAccountValidator> createByAccountValidators = List.of(requestCreateByAccountValidator);
        List<RequestCreateByRequestValidator<TestRequestCreateActionPayload>> createByRequestValidators = List.of();

        RequestCreateActionAccountResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionResourceTypeHandler = new RequestCreateActionAccountResourceTypeHandler<>(createByAccountValidators, createByRequestValidators, handlers, accountQueryService);

        BusinessException businessException = assertThrows(BusinessException.class, () -> requestCreateActionResourceTypeHandler.process("1", "requestType1", testRequestCreateActionPayload, appUser));

        assertEquals(ErrorCode.REQUEST_CREATE_ACTION_NOT_ALLOWED, businessException.getErrorCode());
        assertTrue(Arrays.asList(businessException.getData()).contains(RequestCreateValidationResult.builder()
                .valid(true)
                .isAvailable(false)
                .build()));
        verify(accountQueryService, times(1)).exclusiveLockAccount(1L);
    }

    @Test
    void process_handler_not_found() {
        requestCreateByAccountValidator = new RequestCreateByAccountValidator() {

            @Override
            public String getRequestType() {
                return "requestType1";
            }

            @Override
            public RequestCreateValidationResult validateAction(Long accountId) {
                return RequestCreateValidationResult.builder()
                        .valid(true)
                        .build();
            }
        };

        requestAccountCreateActionHandler = new RequestAccountCreateActionHandler<>() {

            @Override
            public String process(Long accountId, TestRequestCreateActionPayload payload, AppUser appUser) {
                return "requestId";
            }

            @Override
            public String getRequestType() {
                return "requestType2";
            }
        };

        TestRequestCreateActionPayload testRequestCreateActionPayload = TestRequestCreateActionPayload.builder()
                .payloadType("PAYLOAD_TYPE")
                .build();

        AppUser appUser = AppUser.builder().build();

        List<RequestAccountCreateActionHandler<TestRequestCreateActionPayload>> handlers = List.of(requestAccountCreateActionHandler);
        List<RequestCreateByAccountValidator> createByAccountValidators = List.of(requestCreateByAccountValidator);
        List<RequestCreateByRequestValidator<TestRequestCreateActionPayload>> createByRequestValidators = List.of();

        RequestCreateActionAccountResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionResourceTypeHandler = new RequestCreateActionAccountResourceTypeHandler<>(createByAccountValidators, createByRequestValidators, handlers, accountQueryService);
        BusinessException businessException = assertThrows(BusinessException.class, () -> requestCreateActionResourceTypeHandler.process("1", "requestType1", testRequestCreateActionPayload, appUser));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, businessException.getErrorCode());
        assertTrue(Arrays.asList(businessException.getData()).contains("requestType1"));

        verify(accountQueryService, times(1)).exclusiveLockAccount(1L);
    }

    @Test
    void process_resourceId_invalid_type() {
        RequestCreateActionAccountResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionResourceTypeHandler = new RequestCreateActionAccountResourceTypeHandler<>(List.of(), List.of(), List.of(), accountQueryService);
        assertThrows(NumberFormatException.class, () -> requestCreateActionResourceTypeHandler.process("abc", "requestType1", null, null));
    }

    @Test
    void process_createByRequest_validator_exists() {
        requestCreateByRequestValidator = new RequestCreateByRequestValidator<>() {

            @Override
            public RequestCreateValidationResult validateAction(Long accountId, RequestCreateActionPayload payload) {
                return RequestCreateValidationResult.builder()
                        .valid(true)
                        .build();
            }

            @Override
            public String getRequestType() {
                return "requestType1";
            }
        };

        requestAccountCreateActionHandler = new RequestAccountCreateActionHandler<>() {

            @Override
            public String process(Long accountId, TestRequestCreateActionPayload payload, AppUser appUser) {
                return "requestId";
            }

            @Override
            public String getRequestType() {
                return "requestType1";
            }
        };

        TestRequestCreateActionPayload testRequestCreateActionPayload = TestRequestCreateActionPayload.builder()
                .payloadType("PAYLOAD_TYPE")
                .build();

        AppUser appUser = AppUser.builder().build();

        List<RequestAccountCreateActionHandler<TestRequestCreateActionPayload>> handlers = List.of(requestAccountCreateActionHandler);
        List<RequestCreateByAccountValidator> createByAccountValidators = List.of();
        List<RequestCreateByRequestValidator<TestRequestCreateActionPayload>> createByRequestValidators =
                List.of(requestCreateByRequestValidator);

        RequestCreateActionAccountResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionResourceTypeHandler =
                new RequestCreateActionAccountResourceTypeHandler<>(createByAccountValidators,createByRequestValidators, handlers, accountQueryService);
        String requestId = requestCreateActionResourceTypeHandler.process("1", "requestType1", testRequestCreateActionPayload, appUser);

        assertEquals(requestId, "requestId");

        verify(accountQueryService, times(1)).exclusiveLockAccount(1L);
    }

}