package uk.gov.netz.api.workflow.request.flow.rfi.handler;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
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
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskPayloadTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestService;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.TestRequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestAccountContactQueryService;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestActionUserInfoResolver;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiQuestionPayload;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiSubmitPayload;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiSubmitRequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiSubmittedRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.rfi.mapper.RfiMapper;
import uk.gov.netz.api.workflow.request.flow.rfi.service.RfiSendEventService;
import uk.gov.netz.api.workflow.request.flow.rfi.service.RfiSubmitOfficialNoticeService;
import uk.gov.netz.api.workflow.request.flow.rfi.validation.SubmitRfiValidatorService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RfiSubmitActionHandlerTest {

    private static final RfiMapper RFI_MAPPER = Mappers.getMapper(RfiMapper.class);

    @InjectMocks
    private RfiSubmitActionHandler handler;

    @Mock
    private RequestService requestService;

    @Mock
    private RequestTaskService requestTaskService;

    @Mock
    private RfiSendEventService rfiSendEventService;

    @Mock
    private SubmitRfiValidatorService validator;

    @Mock
    private RequestActionUserInfoResolver requestActionUserInfoResolver;

    @Mock
    private UserAuthService userAuthService;

    @Mock
    private RequestAccountContactQueryService requestAccountContactQueryService;

    @Mock
    private RfiSubmitOfficialNoticeService rfiSubmitOfficialNoticeService;

    @Test
    void process() {

        final Long requestTaskId = 1L;
        final AppUser pmrvUser = AppUser.builder().userId("userId").build();

        final UUID referencedFile = UUID.randomUUID();
        final UUID unreferencedFile = UUID.randomUUID();

        final LocalDate deadline = LocalDate.of(2023, 1, 1);
        final RfiQuestionPayload rfiQuestionPayload = RfiQuestionPayload.builder()
                .questions(List.of("what", "when", "how"))
                .files(Set.of(referencedFile)).build();
        final RfiSubmitPayload rfiSubmitPayload = RfiSubmitPayload.builder().rfiQuestionPayload(
                        rfiQuestionPayload)
                .deadline(deadline)
                .operators(Set.of("operator"))
                .signatory("signatory")
                .build();

        final RfiSubmitRequestTaskActionPayload taskActionPayload = RfiSubmitRequestTaskActionPayload.builder()
                .rfiSubmitPayload(rfiSubmitPayload)
                .build();

        final Map<UUID, String> rfiAttachments = new HashMap<>();
        rfiAttachments.put(referencedFile, "rfiFileReferenced");
        rfiAttachments.put(unreferencedFile, "rfiFileUnreferenced");
        final TestRequestTaskPayload requestTaskPayload =
                TestRequestTaskPayload.builder()
                        .payloadType(RequestTaskPayloadTypes.PAYMENT_MAKE_PAYLOAD)
                        .rfiAttachments(rfiAttachments)
                        .build();

        final TestRequestPayload requestPayload = TestRequestPayload.builder().build();
        final RequestTask requestTask = RequestTask.builder()
                .id(requestTaskId)
                .request(Request.builder().payload(requestPayload).id("2").build())
                .type(RequestTaskType.builder().code("DUMMY_REQUEST_TASK_TYPE2").build())
                .payload(requestTaskPayload)
                .processTaskId("processTaskId")
                .build();

        final FileInfoDTO officialNotice = FileInfoDTO.builder()
                .name("off_doc.pdf")
                .uuid(UUID.randomUUID().toString())
                .build();

        final RfiSubmittedRequestActionPayload timelinePayload =
                RFI_MAPPER.toRfiSubmittedRequestActionPayload(taskActionPayload);
        timelinePayload.setRfiAttachments(Map.of(referencedFile, "rfiFileReferenced"));
        timelinePayload.setOfficialDocument(officialNotice);

        final String accountPrimaryContactUserId = "primaryUserId";
        UserInfoDTO accountPrimaryContact = UserInfoDTO.builder()
                .firstName("fn").lastName("ln").email("email@email").userId(accountPrimaryContactUserId)
                .build();
        UserInfo operator = UserInfo.builder()
                .firstName("fn_operator").lastName("ln_operator").email("operator@email")
                .build();

        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        doNothing().when(validator).validate(requestTask, rfiSubmitPayload, pmrvUser);
        doNothing().when(rfiSendEventService).send("2", deadline);
        when(requestActionUserInfoResolver.getUsersInfo(any(), anyString(), any())).thenReturn(Map.of());
        when(requestAccountContactQueryService.getRequestAccountPrimaryContact(requestTask.getRequest()))
                .thenReturn(Optional.of(accountPrimaryContact));
        when(userAuthService.getUsers(new ArrayList<>(rfiSubmitPayload.getOperators()))).thenReturn(List.of(operator));
        when(rfiSubmitOfficialNoticeService.generateOfficialNotice(requestTask.getRequest(), rfiSubmitPayload.getSignatory(), accountPrimaryContact, List.of(operator.getEmail())))
                .thenReturn(officialNotice);

        RequestTaskPayload taskPayload = handler.process(requestTaskId, RequestTaskActionTypes.RFI_SUBMIT, pmrvUser, taskActionPayload);

        assertThat(taskPayload).isEqualTo(requestTaskPayload);
        assertThat(requestTaskPayload.getRfiAttachments()).isEmpty();
        assertThat(requestPayload.getRfiData().getRfiQuestionPayload()).isEqualTo(rfiQuestionPayload);
        assertThat(requestPayload.getRfiData().getRfiAttachments()).containsExactlyInAnyOrderEntriesOf(
                Map.of(referencedFile, "rfiFileReferenced", unreferencedFile, "rfiFileUnreferenced"));

        verify(requestTaskService, times(1)).findTaskById(requestTaskId);
        verify(validator, times(1)).validate(requestTask, rfiSubmitPayload, pmrvUser);
        verify(requestAccountContactQueryService, times(1)).getRequestAccountPrimaryContact(requestTask.getRequest());
        verify(userAuthService, times(1)).getUsers(new ArrayList<>(rfiSubmitPayload.getOperators()));
        verify(rfiSubmitOfficialNoticeService, times(1)).generateOfficialNotice(requestTask.getRequest(), rfiSubmitPayload.getSignatory(), accountPrimaryContact, List.of(operator.getEmail()));
        verify(requestActionUserInfoResolver, times(1))
                .getUsersInfo(rfiSubmitPayload.getOperators(), rfiSubmitPayload.getSignatory(), requestTask.getRequest());
        verify(requestService, times(1))
                .addActionToRequest(requestTask.getRequest(),
                        timelinePayload,
                        RequestActionTypes.RFI_SUBMITTED,
                        pmrvUser.getUserId());
        verify(rfiSendEventService, times(1)).send(requestTask.getRequest().getId(), deadline);
        verify(rfiSubmitOfficialNoticeService, times(1)).sendOfficialNotice(officialNotice, requestTask.getRequest(), List.of(operator.getEmail()));
    }

    @Test
    void getTypes() {
        assertThat(handler.getTypes()).containsExactly(RequestTaskActionTypes.RFI_SUBMIT);
    }
}
