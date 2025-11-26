package uk.gov.netz.api.workflow.request.flow.rde.handler;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.userinfoapi.UserInfo;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestService;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.actionhandler.RequestTaskActionHandler;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestActionUserInfo;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestAccountContactQueryService;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestActionUserInfoResolver;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeData;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdePayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeSubmitRequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeSubmittedRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RequestPayloadRdeable;
import uk.gov.netz.api.workflow.request.flow.rde.mapper.RdeMapper;
import uk.gov.netz.api.workflow.request.flow.rde.service.RdeSendEventService;
import uk.gov.netz.api.workflow.request.flow.rde.service.RdeSubmitOfficialNoticeService;
import uk.gov.netz.api.workflow.request.flow.rde.validation.SubmitRdeValidatorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RdeSubmitActionHandler implements RequestTaskActionHandler<RdeSubmitRequestTaskActionPayload> {

    private final RequestTaskService requestTaskService;
    private final RequestAccountContactQueryService requestAccountContactQueryService;
    private final UserInfoApi userInfoApi;
    private final SubmitRdeValidatorService validator;
    private final RequestActionUserInfoResolver requestActionUserInfoResolver;
    private final RequestService requestService;
    private final RdeSendEventService rdeSendEventService;
    private final RdeSubmitOfficialNoticeService rdeSubmitOfficialNoticeService;
    
    private static final RdeMapper rdeMapper = Mappers.getMapper(RdeMapper.class);

    @Override
    public RequestTaskPayload process(Long requestTaskId, String requestTaskActionType, AppUser appUser,
                                      RdeSubmitRequestTaskActionPayload actionPayload) {

        final RequestTask requestTask = requestTaskService.findTaskById(requestTaskId);
        final RdePayload rdePayload = actionPayload.getRdePayload();

        // Validate
        validator.validate(requestTask, rdePayload, appUser);

        // Copy RDE request in request payload
        final Request request = requestTask.getRequest();
        final RequestPayloadRdeable requestPayload = (RequestPayloadRdeable) request.getPayload();
        requestPayload.setRdeData(RdeData.builder()
                .rdePayload(rdePayload)
                .currentDueDate(requestTask.getDueDate())
                .build());
        
        final UserInfoDTO accountPrimaryContact = requestAccountContactQueryService.getRequestAccountPrimaryContact(request)
            .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_CONTACT_TYPE_PRIMARY_CONTACT_NOT_FOUND));
        
        final List<String> ccRecipientsEmails = userInfoApi.getUsers(new ArrayList<>(rdePayload.getOperators()))
                .stream().map(UserInfo::getEmail).collect(Collectors.toList());
        
        //generate official document file
        final FileInfoDTO officialDocument = rdeSubmitOfficialNoticeService.generateOfficialNotice(request,
                rdePayload.getSignatory(), accountPrimaryContact, ccRecipientsEmails);

        // Get users' information
        final Map<String, RequestActionUserInfo> usersInfo = requestActionUserInfoResolver
            .getUsersInfo(rdePayload.getOperators(), rdePayload.getSignatory(), request);

        // Create timeline action
        RdeSubmittedRequestActionPayload timelinePayload = rdeMapper.toRdeSubmittedRequestActionPayload(actionPayload, usersInfo, officialDocument);
        requestService.addActionToRequest(request, timelinePayload, RequestActionTypes.RDE_SUBMITTED, appUser.getUserId());

        // Send RDE event
        rdeSendEventService.send(request.getId(), rdePayload.getDeadline());
        
        //send email notification
        rdeSubmitOfficialNoticeService.sendOfficialNotice(officialDocument, request, ccRecipientsEmails);

        return requestTask.getPayload();
    }

    @Override
    public List<String> getTypes() {
        return List.of(RequestTaskActionTypes.RDE_SUBMIT);
    }
}
