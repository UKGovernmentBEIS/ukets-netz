package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.competentauthority.CompetentAuthorityService;
import uk.gov.netz.api.workflow.request.TestRequestCreateActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestCreateActionPayload;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestCreateByAccountValidator;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestCreateByCAValidator;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestCreateActionCAResourceTypeHandlerTest {
    @Mock
    private CompetentAuthorityService competentAuthorityService;

    @Mock
    private RequestCreateByCAValidator<TestRequestCreateActionPayload> requestCreateByCAValidator;

    @Mock
    private RequestCACreateActionHandler<TestRequestCreateActionPayload> requestCACreateActionHandler;

    @Test
    void process() {
        requestCreateByCAValidator = new RequestCreateByCAValidator<>() {

            @Override
            public RequestCreateValidationResult validateAction(CompetentAuthorityEnum competentAuthority, TestRequestCreateActionPayload payload) {
                return RequestCreateValidationResult.builder()
                        .valid(true)
                        .build();
            }

            @Override
            public String getRequestType() {
                return "requestType1";
            }
        };

        requestCACreateActionHandler = new RequestCACreateActionHandler<>() {

            @Override
            public String process(CompetentAuthorityEnum ca, TestRequestCreateActionPayload payload, AppUser appUser) {
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

        List<RequestCreateByCAValidator<TestRequestCreateActionPayload>> validators = List.of(requestCreateByCAValidator);
        List<RequestCACreateActionHandler<TestRequestCreateActionPayload>> handlers = List.of(requestCACreateActionHandler);

        RequestCreateActionCAResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionCAResourceTypeHandler = new RequestCreateActionCAResourceTypeHandler<>(validators, handlers, competentAuthorityService);
        String requestId = requestCreateActionCAResourceTypeHandler.process("ENGLAND", "requestType1", testRequestCreateActionPayload, appUser);

        assertEquals(requestId, "requestId");

        verify(competentAuthorityService, times(1)).exclusiveLockCompetentAuthority(CompetentAuthorityEnum.ENGLAND);
    }

    @Test
    void process_validator_not_found() {
        requestCreateByCAValidator = new RequestCreateByCAValidator<>() {

            @Override
            public String getRequestType() {
                return "requestType2";
            }

            @Override
            public RequestCreateValidationResult validateAction(CompetentAuthorityEnum competentAuthority, TestRequestCreateActionPayload payload) {
                return RequestCreateValidationResult.builder()
                        .valid(true)
                        .build();
            }
        };

        requestCACreateActionHandler = new RequestCACreateActionHandler<>() {

            @Override
            public String process(CompetentAuthorityEnum ca, TestRequestCreateActionPayload payload, AppUser appUser) {
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

        List<RequestCreateByCAValidator<TestRequestCreateActionPayload>> validators = List.of(requestCreateByCAValidator);
        List<RequestCACreateActionHandler<TestRequestCreateActionPayload>> handlers = List.of(requestCACreateActionHandler);

        RequestCreateActionCAResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionCAResourceTypeHandler = new RequestCreateActionCAResourceTypeHandler<>(validators, handlers, competentAuthorityService);
        String requestId = requestCreateActionCAResourceTypeHandler.process("ENGLAND", "requestType1", testRequestCreateActionPayload, appUser);

        assertEquals(requestId, "requestId");

        verify(competentAuthorityService, times(1)).exclusiveLockCompetentAuthority(CompetentAuthorityEnum.ENGLAND);
    }

    @Test
    void process_validator_invalid() {
        requestCreateByCAValidator = new RequestCreateByCAValidator<>() {

            @Override
            public String getRequestType() {
                return "requestType1";
            }

            @Override
            public RequestCreateValidationResult validateAction(CompetentAuthorityEnum competentAuthority, TestRequestCreateActionPayload payload) {
                return RequestCreateValidationResult.builder()
                        .valid(false)
                        .build();
            }
        };

        TestRequestCreateActionPayload testRequestCreateActionPayload = TestRequestCreateActionPayload.builder()
                .payloadType("PAYLOAD_TYPE")
                .build();

        AppUser appUser = AppUser.builder().build();

        List<RequestCreateByCAValidator<TestRequestCreateActionPayload>> validators = List.of(requestCreateByCAValidator);
        List<RequestCACreateActionHandler<TestRequestCreateActionPayload>> handlers = List.of();

        RequestCreateActionCAResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionCAResourceTypeHandler = new RequestCreateActionCAResourceTypeHandler<>(validators, handlers, competentAuthorityService);

        BusinessException businessException = assertThrows(BusinessException.class, () -> requestCreateActionCAResourceTypeHandler.process("ENGLAND", "requestType1", testRequestCreateActionPayload, appUser));

        assertEquals(ErrorCode.REQUEST_CREATE_ACTION_NOT_ALLOWED, businessException.getErrorCode());
        assertTrue(Arrays.asList(businessException.getData()).contains(RequestCreateValidationResult.builder()
                .valid(false)
                .build()));
        verify(competentAuthorityService, times(1)).exclusiveLockCompetentAuthority(CompetentAuthorityEnum.ENGLAND);
    }

    @Test
    void process_validator_not_available() {
        requestCreateByCAValidator = new RequestCreateByCAValidator<>() {

            @Override
            public String getRequestType() {
                return "requestType1";
            }

            @Override
            public RequestCreateValidationResult validateAction(CompetentAuthorityEnum competentAuthority, TestRequestCreateActionPayload payload) {
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

        List<RequestCreateByCAValidator<TestRequestCreateActionPayload>> validators = List.of(requestCreateByCAValidator);
        List<RequestCACreateActionHandler<TestRequestCreateActionPayload>> handlers = List.of();

        RequestCreateActionCAResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionCAResourceTypeHandler = new RequestCreateActionCAResourceTypeHandler<>(validators, handlers, competentAuthorityService);

        BusinessException businessException = assertThrows(BusinessException.class, () -> requestCreateActionCAResourceTypeHandler.process("ENGLAND", "requestType1", testRequestCreateActionPayload, appUser));

        assertEquals(ErrorCode.REQUEST_CREATE_ACTION_NOT_ALLOWED, businessException.getErrorCode());
        assertTrue(Arrays.asList(businessException.getData()).contains(RequestCreateValidationResult.builder()
                .valid(true)
                .isAvailable(false)
                .build()));
        verify(competentAuthorityService, times(1)).exclusiveLockCompetentAuthority(CompetentAuthorityEnum.ENGLAND);
    }

    @Test
    void process_handler_not_found() {
        requestCreateByCAValidator = new RequestCreateByCAValidator<>() {

            @Override
            public String getRequestType() {
                return "requestType1";
            }

            @Override
            public RequestCreateValidationResult validateAction(CompetentAuthorityEnum competentAuthority, TestRequestCreateActionPayload payload) {
                return RequestCreateValidationResult.builder()
                        .valid(true)
                        .build();
            }
        };

        requestCACreateActionHandler = new RequestCACreateActionHandler<>() {

            @Override
            public String process(CompetentAuthorityEnum ca, TestRequestCreateActionPayload payload, AppUser appUser) {
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

        List<RequestCreateByCAValidator<TestRequestCreateActionPayload>> validators = List.of(requestCreateByCAValidator);
        List<RequestCACreateActionHandler<TestRequestCreateActionPayload>> handlers = List.of();

        RequestCreateActionCAResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionCAResourceTypeHandler = new RequestCreateActionCAResourceTypeHandler<>(validators, handlers, competentAuthorityService);
        BusinessException businessException = assertThrows(BusinessException.class, () -> requestCreateActionCAResourceTypeHandler.process("ENGLAND", "requestType1", testRequestCreateActionPayload, appUser));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, businessException.getErrorCode());
        assertTrue(Arrays.asList(businessException.getData()).contains("requestType1"));

        verify(competentAuthorityService, times(1)).exclusiveLockCompetentAuthority(CompetentAuthorityEnum.ENGLAND);
    }

    @Test
    void process_resourceId_invalid_type() {
        RequestCreateActionCAResourceTypeHandler<TestRequestCreateActionPayload> requestCreateActionCAResourceTypeHandler = new RequestCreateActionCAResourceTypeHandler<>(List.of(), List.of(), competentAuthorityService);
        assertThrows(IllegalArgumentException.class, () -> requestCreateActionCAResourceTypeHandler.process("1", "requestType1", null, null));
    }
}