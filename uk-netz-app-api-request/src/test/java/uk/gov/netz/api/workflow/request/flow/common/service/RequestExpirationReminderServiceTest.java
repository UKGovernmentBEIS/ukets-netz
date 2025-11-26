package uk.gov.netz.api.workflow.request.flow.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.domain.dto.AccountInfoDTO;
import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityDTO;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.competentauthority.CompetentAuthorityService;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.service.NotificationEmailService;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.service.RequestService;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.NotificationTemplateExpirationReminderParams;
import uk.gov.netz.api.workflow.utils.NotificationTemplateConstants;
import uk.gov.netz.api.workflow.utils.NotificationTemplateName;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestExpirationReminderServiceTest {

    @InjectMocks
    private RequestExpirationReminderService service;

    @Mock
    private RequestService requestService;
    
    @Mock
    private AccountQueryService accountQueryService;

    @Mock
    private NotificationEmailService<EmailNotificationTemplateData> notificationEmailService;

    @Mock
    private CompetentAuthorityService competentAuthorityService;

    @Mock
    private RequestAccountContactQueryService requestAccountContactQueryService;

    @Test
    void sendExpirationReminderNotification_has_not_primary_and_service_contact() {
        String requestId = "1";
        Long accountId = 1L;
        Date deadline = new Date();
        CompetentAuthorityDTO ca = CompetentAuthorityDTO.builder().id(CompetentAuthorityEnum.ENGLAND).email("email").build();

        NotificationTemplateExpirationReminderParams expirationParams = NotificationTemplateExpirationReminderParams.builder()
                .workflowTask("request for information")
                .recipient(UserInfoDTO.builder()
                        .email("recipient@email")
                        .firstName("fn").lastName("ln")
                        .build())
                .expirationTime("1 day")
                .expirationTimeLong("in one day")
                .deadline(deadline)
                .build();
        
        Request request = Request.builder()
                .id(requestId)
                .type(RequestType.builder().code("DUMMY_REQUEST_TYPE").build())
                .build();
        addResourcesToRequest(accountId, CompetentAuthorityEnum.ENGLAND, request);
        String businessId = "businessId";
        AccountInfoDTO account = AccountInfoDTO.builder()
                .id(accountId)
                .name("account name")
                .businessId(businessId)
                .build();
        
        when(requestService.findRequestById(requestId)).thenReturn(request);
        when(accountQueryService.getAccountInfoDTOById(accountId)).thenReturn(account);
        when(competentAuthorityService.getCompetentAuthorityDTO(CompetentAuthorityEnum.ENGLAND)).thenReturn(ca);

        //invoke
        service.sendExpirationReminderNotification(requestId, expirationParams);
        
        verify(requestService, times(1)).findRequestById(requestId);
        verify(accountQueryService, times(1)).getAccountInfoDTOById(accountId);

        ArgumentCaptor<EmailData<EmailNotificationTemplateData>> emailDataCaptor = ArgumentCaptor.forClass(EmailData.class);
        
        verify(notificationEmailService, times(1)).notifyRecipient(emailDataCaptor.capture(), Mockito.eq("recipient@email"));
        EmailData<EmailNotificationTemplateData> emailDataCaptured = emailDataCaptor.getValue();
        
        final Map<String, Object> expectedTemplateParams = new HashMap<>();
        expectedTemplateParams.put(NotificationTemplateConstants.ACCOUNT_NAME, account.getName());
        expectedTemplateParams.put(NotificationTemplateConstants.ACCOUNT_BUSINESS_ID, businessId);
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW_ID, request.getId());
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW, request.getType().getDescription());
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW_TASK, expirationParams.getWorkflowTask());
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW_USER, expirationParams.getRecipient().getFullName());
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW_EXPIRATION_TIME, expirationParams.getExpirationTime());
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW_EXPIRATION_TIME_LONG, expirationParams.getExpirationTimeLong());
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW_DEADLINE, expirationParams.getDeadline());
        expectedTemplateParams.put(NotificationTemplateConstants.COMPETENT_AUTHORITY_EMAIL, ca.getEmail());
        
        assertThat(emailDataCaptured).isEqualTo(EmailData.builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder()
                        .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                        .templateName(NotificationTemplateName.GENERIC_EXPIRATION_REMINDER)
                        .templateParams(expectedTemplateParams)
                        .build())
                .build());  
    }

    @Test
    void sendExpirationReminderNotification_has_primary_and_service_contacts() {
        String requestId = "1";
        Long accountId = 1L;
        Date deadline = new Date();
        CompetentAuthorityDTO ca = CompetentAuthorityDTO.builder().id(CompetentAuthorityEnum.ENGLAND).email("email").build();

        NotificationTemplateExpirationReminderParams expirationParams = NotificationTemplateExpirationReminderParams.builder()
            .workflowTask("request for information")
            .recipient(UserInfoDTO.builder()
                .email("recipient@email")
                .firstName("fn").lastName("ln")
                .build())
            .expirationTime("1 day")
            .expirationTimeLong("in one day")
            .deadline(deadline)
            .build();

        Request request = Request.builder()
            .id(requestId)
            .type(RequestType.builder().code("DUMMY_REQUEST_TYPE").build())
            .build();
        addResourcesToRequest(accountId, CompetentAuthorityEnum.ENGLAND, request);
        String businessId = "businessId";
        AccountInfoDTO account = AccountInfoDTO.builder()
            .id(accountId)
            .name("account name")
            .businessId(businessId)
            .build();
        UserInfoDTO accountPrimaryContact = UserInfoDTO.builder()
            .firstName("fn").lastName("ln").email("primary@email").userId("primaryUserId")
            .build();
        UserInfoDTO accountServiceContact = UserInfoDTO.builder()
            .firstName("ab").lastName("cd").email("service@email").userId("serviceUserId")
            .build();

        when(requestService.findRequestById(requestId)).thenReturn(request);
        when(accountQueryService.getAccountInfoDTOById(accountId)).thenReturn(account);
        when(competentAuthorityService.getCompetentAuthorityDTO(CompetentAuthorityEnum.ENGLAND)).thenReturn(ca);
        when(requestAccountContactQueryService.getRequestAccountPrimaryContact(request))
            .thenReturn(Optional.of(accountPrimaryContact));
        when(requestAccountContactQueryService.getRequestAccountServiceContact(request))
            .thenReturn(Optional.of(accountServiceContact));

        //invoke
        service.sendExpirationReminderNotification(requestId, expirationParams);

        verify(requestService, times(1)).findRequestById(requestId);
        verify(accountQueryService, times(1)).getAccountInfoDTOById(accountId);
        verify(requestAccountContactQueryService).getRequestAccountPrimaryContact(request);
        verify(requestAccountContactQueryService).getRequestAccountServiceContact(request);

        ArgumentCaptor<EmailData<EmailNotificationTemplateData>> emailDataCaptor = ArgumentCaptor.forClass(EmailData.class);

        verify(notificationEmailService, times(1)).notifyRecipient(emailDataCaptor.capture(), Mockito.eq("recipient@email"));
        EmailData<EmailNotificationTemplateData> emailDataCaptured = emailDataCaptor.getValue();

        final Map<String, Object> expectedTemplateParams = new HashMap<>();
        expectedTemplateParams.put(NotificationTemplateConstants.ACCOUNT_NAME, account.getName());
        expectedTemplateParams.put(NotificationTemplateConstants.ACCOUNT_BUSINESS_ID, businessId);
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW_ID, request.getId());
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW, request.getType().getDescription());
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW_TASK, expirationParams.getWorkflowTask());
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW_USER, expirationParams.getRecipient().getFullName());
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW_EXPIRATION_TIME, expirationParams.getExpirationTime());
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW_EXPIRATION_TIME_LONG, expirationParams.getExpirationTimeLong());
        expectedTemplateParams.put(NotificationTemplateConstants.WORKFLOW_DEADLINE, expirationParams.getDeadline());
        expectedTemplateParams.put(NotificationTemplateConstants.COMPETENT_AUTHORITY_EMAIL, ca.getEmail());
        expectedTemplateParams.put(NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT, accountPrimaryContact.getFullName());
        expectedTemplateParams.put(NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_FIRST_NAME, accountPrimaryContact.getFirstName());
        expectedTemplateParams.put(NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_LAST_NAME, accountPrimaryContact.getLastName());
        expectedTemplateParams.put(NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT, accountServiceContact.getFullName());
        expectedTemplateParams.put(NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_FIRST_NAME, accountServiceContact.getFirstName());
        expectedTemplateParams.put(NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_LAST_NAME, accountServiceContact.getLastName());

        assertThat(emailDataCaptured).isEqualTo(EmailData.builder()
            .notificationTemplateData(EmailNotificationTemplateData.builder()
                .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                .templateName(NotificationTemplateName.GENERIC_EXPIRATION_REMINDER)
                .templateParams(expectedTemplateParams)
                .build())
            .build());

        verifyNoMoreInteractions(requestService, accountQueryService,
            requestAccountContactQueryService, notificationEmailService);
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
