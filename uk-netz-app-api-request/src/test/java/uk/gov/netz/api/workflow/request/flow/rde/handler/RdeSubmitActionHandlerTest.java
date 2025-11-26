package uk.gov.netz.api.workflow.request.flow.rde.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.user.core.service.auth.UserAuthService;
import uk.gov.netz.api.userinfoapi.UserInfo;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.TestRequestPayload;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskPayloadTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestService;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestActionUserInfo;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestAccountContactQueryService;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestActionUserInfoResolver;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentMakeRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdePayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeSubmitRequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeSubmittedRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.rde.service.RdeSendEventService;
import uk.gov.netz.api.workflow.request.flow.rde.service.RdeSubmitOfficialNoticeService;
import uk.gov.netz.api.workflow.request.flow.rde.validation.SubmitRdeValidatorService;

@ExtendWith(MockitoExtension.class)
class RdeSubmitActionHandlerTest {

    @InjectMocks
    private RdeSubmitActionHandler handler;

    @Mock
    private RequestTaskService requestTaskService;

    @Mock
    private RequestAccountContactQueryService requestAccountContactQueryService;

    @Mock
    private UserAuthService userAuthService;

    @Mock
    private SubmitRdeValidatorService validator;

    @Mock
    private RequestActionUserInfoResolver requestActionUserInfoResolver;

    @Mock
    private RequestService requestService;

    @Mock
    private RdeSendEventService rdeSendEventService;

    @Mock
    private RdeSubmitOfficialNoticeService rdeSubmitOfficialNoticeService;

    @Test
    void process() {
        final Long requestTaskId = 1L;
        final String requestId = "2";
        final LocalDate dueDate = LocalDate.now().plusDays(5);
        final AppUser pmrvUser = AppUser.builder().userId("userId").build();

        final RdePayload rdePayload = RdePayload.builder()
                .extensionDate(LocalDate.now().plusDays(10))
                .deadline(LocalDate.now().plusDays(1))
                .operators(Set.of("operator"))
                .signatory("signatory")
                .build();
        final RdeSubmitRequestTaskActionPayload taskActionPayload = RdeSubmitRequestTaskActionPayload.builder()
                .rdePayload(rdePayload)
                .build();

        final UserInfo operator = UserInfo.builder()
                .firstName("fn_operator").lastName("ln_operator").email("operator@email")
                .build();

        final Map<String, RequestActionUserInfo> usersInfo = Map.of(
                "operator", RequestActionUserInfo.builder().name("operator").roleCode("operator").build(),
                "signatory", RequestActionUserInfo.builder().name("signatory").roleCode("signatory").build()
        );

        final TestRequestPayload requestPayload = TestRequestPayload.builder().build();
        final RequestTaskPayload requestTaskPayload =
                PaymentMakeRequestTaskPayload.builder()
                        .payloadType(RequestTaskPayloadTypes.PAYMENT_MAKE_PAYLOAD)
                        .build();
        final Request request = Request.builder().payload(requestPayload).id(requestId).build();
        final RequestTask requestTask = RequestTask.builder()
                .id(requestTaskId)
                .request(request)
                .type(RequestTaskType.builder().code("DUMMY_REQUEST_TYPE_APPLICATION_REVIEW").build())
                .payload(requestTaskPayload)
                .processTaskId("processTaskId")
                .dueDate(dueDate)
                .build();

        final FileInfoDTO officialDocument = FileInfoDTO.builder()
                .name("off_doc.pdf")
                .uuid(UUID.randomUUID().toString())
                .build();

        final RdeSubmittedRequestActionPayload timelinePayload = RdeSubmittedRequestActionPayload.builder()
                .payloadType(RequestActionPayloadTypes.RDE_SUBMITTED_PAYLOAD)
                .rdePayload(rdePayload)
                .usersInfo(usersInfo)
                .officialDocument(officialDocument)
                .build();

        final String accountPrimaryContactUserId = "primaryUserId";
        final UserInfoDTO accountPrimaryContact = UserInfoDTO.builder()
                .firstName("fn").lastName("ln").email("email@email").userId(accountPrimaryContactUserId)
                .build();

        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        when(requestAccountContactQueryService.getRequestAccountPrimaryContact(requestTask.getRequest()))
                .thenReturn(Optional.of(accountPrimaryContact));
        when(userAuthService.getUsers(new ArrayList<>(rdePayload.getOperators()))).thenReturn(List.of(operator));
        when(rdeSubmitOfficialNoticeService.generateOfficialNotice(requestTask.getRequest(), rdePayload.getSignatory(), accountPrimaryContact, List.of(operator.getEmail())))
                .thenReturn(officialDocument);
        when(requestActionUserInfoResolver.getUsersInfo(rdePayload.getOperators(), rdePayload.getSignatory(), request)).thenReturn(usersInfo);

        // Invoke
        RequestTaskPayload taskPayload = handler.process(requestTaskId, RequestTaskActionTypes.RDE_SUBMIT, pmrvUser, taskActionPayload);

        // Verify
        assertThat(taskPayload).isEqualTo(requestTaskPayload);
        assertThat(requestPayload.getRdeData().getRdePayload()).isEqualTo(rdePayload);
        assertThat(requestPayload.getRdeData().getCurrentDueDate()).isEqualTo(dueDate);

        verify(requestTaskService, times(1)).findTaskById(requestTaskId);
        verify(validator, times(1)).validate(requestTask, rdePayload, pmrvUser);
        verify(requestAccountContactQueryService, times(1)).getRequestAccountPrimaryContact(requestTask.getRequest());
        verify(userAuthService, times(1)).getUsers(new ArrayList<>(rdePayload.getOperators()));
        verify(rdeSubmitOfficialNoticeService, times(1)).generateOfficialNotice(requestTask.getRequest(), rdePayload.getSignatory(), accountPrimaryContact, List.of(operator.getEmail()));
        verify(requestActionUserInfoResolver, times(1))
                .getUsersInfo(rdePayload.getOperators(), rdePayload.getSignatory(), request);
        verify(requestService, times(1))
                .addActionToRequest(requestTask.getRequest(), timelinePayload, RequestActionTypes.RDE_SUBMITTED, pmrvUser.getUserId());
        verify(rdeSendEventService, times(1)).send(requestId, rdePayload.getDeadline());
        verify(rdeSubmitOfficialNoticeService, times(1)).sendOfficialNotice(officialDocument, requestTask.getRequest(), List.of(operator.getEmail()));
    }

    @Test
    void getTypes() {
        assertThat(handler.getTypes()).containsExactly(RequestTaskActionTypes.RDE_SUBMIT);
    }
}
