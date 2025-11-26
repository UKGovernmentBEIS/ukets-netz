package uk.gov.netz.api.workflow.request.flow.common.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityDTO;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.competentauthority.CompetentAuthorityService;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.files.documents.service.FileDocumentService;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.service.NotificationEmailService;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestAccountContactQueryService;
import uk.gov.netz.api.workflow.utils.NotificationTemplateConstants;
import uk.gov.netz.api.workflow.utils.NotificationTemplateName;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfficialNoticeSendServiceTest {

	@InjectMocks
    private OfficialNoticeSendService service;
	
	@Mock
    private RequestAccountContactQueryService requestAccountContactQueryService;
	
	@Mock
    private NotificationEmailService<EmailNotificationTemplateData> notificationEmailService;
    
    @Mock
    private FileDocumentService fileDocumentService;

	@Mock
	private CompetentAuthorityService competentAuthorityService;

	@Test
	void sendOfficialNotice_sameServiceContact() {
		FileInfoDTO officialDocFileInfoDTO = FileInfoDTO.builder()
				.name("offDoc.pdf")
				.uuid(UUID.randomUUID().toString())
				.build();

		Request request = Request.builder()
				.id("1")
				.type(RequestType.builder().code("DUMMY_REQUEST_TYPE").build())
				.build();
		addCaResourceToRequest(CompetentAuthorityEnum.ENGLAND, request);

		UserInfoDTO accountPrimaryContact = UserInfoDTO.builder()
				.firstName("fn").lastName("ln").email("primary@email").userId("primaryUserId")
				.build();

		FileDTO officialDocFileDTO = FileDTO.builder().fileContent("content".getBytes()).build();

		CompetentAuthorityDTO competentAuthority = CompetentAuthorityDTO.builder()
				.id(CompetentAuthorityEnum.ENGLAND)
				.name("competentAuthority")
				.email("competent@authority.com")
				.build();

		when(requestAccountContactQueryService.getRequestAccountPrimaryContact(request))
				.thenReturn(Optional.of(accountPrimaryContact));
		when(requestAccountContactQueryService.getRequestAccountServiceContact(request))
				.thenReturn(Optional.of(accountPrimaryContact));
		when(fileDocumentService.getFileDTO(officialDocFileInfoDTO.getUuid()))
				.thenReturn(officialDocFileDTO);
		when(competentAuthorityService.getCompetentAuthorityDTO(CompetentAuthorityEnum.ENGLAND))
				.thenReturn(competentAuthority);

		service.sendOfficialNotice(List.of(officialDocFileInfoDTO), request);

		verify(requestAccountContactQueryService, times(2)).getRequestAccountPrimaryContact(request);
		verify(requestAccountContactQueryService, times(2)).getRequestAccountServiceContact(request);
		verify(fileDocumentService, times(1)).getFileDTO(officialDocFileInfoDTO.getUuid());
		verify(competentAuthorityService, times(1))
				.getCompetentAuthorityDTO(CompetentAuthorityEnum.ENGLAND);

		ArgumentCaptor<EmailData<EmailNotificationTemplateData>> emailDataCaptor = ArgumentCaptor.forClass(EmailData.class);
		verify(notificationEmailService, times(1)).notifyRecipients(emailDataCaptor.capture(),
				Mockito.eq(List.of(accountPrimaryContact.getEmail())), Mockito.eq(Collections.emptyList()), Mockito.eq(Collections.emptyList()));
		EmailData<EmailNotificationTemplateData> emailDataCaptured = emailDataCaptor.getValue();
		assertThat(emailDataCaptured).isEqualTo(EmailData.builder()
				.notificationTemplateData(EmailNotificationTemplateData.builder()
						.templateName(NotificationTemplateName.GENERIC_EMAIL)
						.competentAuthority(CompetentAuthorityEnum.ENGLAND)
						.templateParams(Map.of(
								NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT, accountPrimaryContact.getFullName(),
								NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_FIRST_NAME, accountPrimaryContact.getFirstName(),
								NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_LAST_NAME, accountPrimaryContact.getLastName(),
								NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT, accountPrimaryContact.getFullName(),
								NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_FIRST_NAME, accountPrimaryContact.getFirstName(),
								NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_LAST_NAME, accountPrimaryContact.getLastName(),
								NotificationTemplateConstants.COMPETENT_AUTHORITY_EMAIL, competentAuthority.getEmail(),
								NotificationTemplateConstants.COMPETENT_AUTHORITY_NAME, competentAuthority.getName()
						))
						.build())
				.attachments(Map.of(officialDocFileInfoDTO.getName(), officialDocFileDTO.getFileContent())).build());
	}

	@Test
	void sendOfficialNotice_with_cc_and_sameServiceContact() {
		FileInfoDTO officialDocFileInfoDTO = FileInfoDTO.builder()
				.name("offDoc.pdf")
				.uuid(UUID.randomUUID().toString())
				.build();

		Request request = Request.builder()
				.id("1")
				.type(RequestType.builder().code("DUMMY_REQUEST_TYPE").build())
				.build();
		addCaResourceToRequest(CompetentAuthorityEnum.ENGLAND, request);

		UserInfoDTO accountPrimaryContact = UserInfoDTO.builder()
				.firstName("fn").lastName("ln").email("primary@email").userId("primaryUserId")
				.build();

		FileDTO officialDocFileDTO = FileDTO.builder().fileContent("content".getBytes()).build();

		List<String> ccRecipientsEmails = List.of("cc1@email", "cc2@email");

		CompetentAuthorityDTO competentAuthority = CompetentAuthorityDTO.builder()
				.id(CompetentAuthorityEnum.ENGLAND)
				.name("competentAuthority")
				.email("competent@authority.com")
				.build();

		when(requestAccountContactQueryService.getRequestAccountPrimaryContact(request))
				.thenReturn(Optional.of(accountPrimaryContact));
		when(requestAccountContactQueryService.getRequestAccountServiceContact(request))
				.thenReturn(Optional.of(accountPrimaryContact));
		when(fileDocumentService.getFileDTO(officialDocFileInfoDTO.getUuid()))
				.thenReturn(officialDocFileDTO);
		when(competentAuthorityService.getCompetentAuthorityDTO(CompetentAuthorityEnum.ENGLAND))
				.thenReturn(competentAuthority);

		service.sendOfficialNotice(List.of(officialDocFileInfoDTO), request, ccRecipientsEmails);

		verify(requestAccountContactQueryService, times(2)).getRequestAccountPrimaryContact(request);
		verify(requestAccountContactQueryService, times(2)).getRequestAccountServiceContact(request);
		verify(fileDocumentService, times(1)).getFileDTO(officialDocFileInfoDTO.getUuid());
		verify(competentAuthorityService, times(1))
				.getCompetentAuthorityDTO(CompetentAuthorityEnum.ENGLAND);

		ArgumentCaptor<EmailData> emailDataCaptor = ArgumentCaptor.forClass(EmailData.class);
		verify(notificationEmailService, times(1)).notifyRecipients(emailDataCaptor.capture(),
				Mockito.eq(List.of(accountPrimaryContact.getEmail())), Mockito.eq(ccRecipientsEmails), Mockito.eq(Collections.emptyList()));
		EmailData emailDataCaptured = emailDataCaptor.getValue();
		assertThat(emailDataCaptured).isEqualTo(EmailData.builder()
				.notificationTemplateData(EmailNotificationTemplateData.builder()
						.templateName(NotificationTemplateName.GENERIC_EMAIL)
						.competentAuthority(CompetentAuthorityEnum.ENGLAND)
						.templateParams(Map.of(
								NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT, accountPrimaryContact.getFullName(),
								NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_FIRST_NAME, accountPrimaryContact.getFirstName(),
								NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_LAST_NAME, accountPrimaryContact.getLastName(),
								NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT, accountPrimaryContact.getFullName(),
								NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_FIRST_NAME, accountPrimaryContact.getFirstName(),
								NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_LAST_NAME, accountPrimaryContact.getLastName(),
								NotificationTemplateConstants.COMPETENT_AUTHORITY_EMAIL, competentAuthority.getEmail(),
								NotificationTemplateConstants.COMPETENT_AUTHORITY_NAME, competentAuthority.getName()
						))
						.build())
				.attachments(Map.of(officialDocFileInfoDTO.getName(), officialDocFileDTO.getFileContent())).build());
	}

	@Test
	void sendOfficialNotice_with_cc_and_differentServiceContact() {
		FileInfoDTO officialDocFileInfoDTO = FileInfoDTO.builder()
				.name("offDoc.pdf")
				.uuid(UUID.randomUUID().toString())
				.build();

		Request request = Request.builder()
				.id("1")
				.type(RequestType.builder().code("DUMMY_REQUEST_TYPE").build())
				.build();
		addCaResourceToRequest(CompetentAuthorityEnum.ENGLAND, request);

		UserInfoDTO accountPrimaryContact = UserInfoDTO.builder()
				.firstName("fn").lastName("ln").email("primary@email").userId("primaryUserId")
				.build();

		UserInfoDTO accountServiceContact = UserInfoDTO.builder()
				.firstName("ab").lastName("cd").email("service@email").userId("serviceUserId")
				.build();

		FileDTO officialDocFileDTO = FileDTO.builder().fileContent("content".getBytes()).build();

		List<String> ccRecipientsEmails = List.of("cc1@email", "cc2@email");

		CompetentAuthorityDTO competentAuthority = CompetentAuthorityDTO.builder()
				.id(CompetentAuthorityEnum.ENGLAND)
				.name("competentAuthority")
				.email("competent@authority.com")
				.build();

		when(requestAccountContactQueryService.getRequestAccountPrimaryContact(request))
				.thenReturn(Optional.of(accountPrimaryContact));
		when(requestAccountContactQueryService.getRequestAccountServiceContact(request))
				.thenReturn(Optional.of(accountServiceContact));
		when(fileDocumentService.getFileDTO(officialDocFileInfoDTO.getUuid()))
				.thenReturn(officialDocFileDTO);
		when(competentAuthorityService.getCompetentAuthorityDTO(CompetentAuthorityEnum.ENGLAND))
				.thenReturn(competentAuthority);

		service.sendOfficialNotice(List.of(officialDocFileInfoDTO), request, ccRecipientsEmails);

		verify(requestAccountContactQueryService, times(2)).getRequestAccountPrimaryContact(request);
		verify(requestAccountContactQueryService, times(2)).getRequestAccountServiceContact(request);
		verify(fileDocumentService, times(1)).getFileDTO(officialDocFileInfoDTO.getUuid());
		verify(competentAuthorityService, times(1))
				.getCompetentAuthorityDTO(CompetentAuthorityEnum.ENGLAND);

		ArgumentCaptor<EmailData> emailDataCaptor = ArgumentCaptor.forClass(EmailData.class);
		ArgumentCaptor<List<String>> toRecipientsEmailsCaptor = ArgumentCaptor.forClass(List.class);
		
		verify(notificationEmailService, times(1)).notifyRecipients(emailDataCaptor.capture(),
				toRecipientsEmailsCaptor.capture(), Mockito.eq(ccRecipientsEmails), Mockito.eq(Collections.emptyList()));
		List<String> toRecipientsEmailsCaptured = toRecipientsEmailsCaptor.getValue();
		assertThat(toRecipientsEmailsCaptured).containsExactlyInAnyOrder("primary@email", "service@email");
		
		EmailData emailDataCaptured = emailDataCaptor.getValue();
		assertThat(emailDataCaptured).isEqualTo(EmailData.builder()
				.notificationTemplateData(EmailNotificationTemplateData.builder()
						.templateName(NotificationTemplateName.GENERIC_EMAIL)
						.competentAuthority(CompetentAuthorityEnum.ENGLAND)
						.templateParams(Map.of(
								NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT, accountPrimaryContact.getFullName(),
								NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_LAST_NAME, accountPrimaryContact.getLastName(),
								NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_FIRST_NAME, accountPrimaryContact.getFirstName(),
								NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT, accountServiceContact.getFullName(),
								NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_FIRST_NAME, accountServiceContact.getFirstName(),
								NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_LAST_NAME, accountServiceContact.getLastName(),
								NotificationTemplateConstants.COMPETENT_AUTHORITY_EMAIL, competentAuthority.getEmail(),
								NotificationTemplateConstants.COMPETENT_AUTHORITY_NAME, competentAuthority.getName()
						))
						.build())
				.attachments(Map.of(officialDocFileInfoDTO.getName(), officialDocFileDTO.getFileContent())).build());
	}

	@Test
	void sendOfficialNotice_with_cc_and_bcc_whenDuplicatesInCc_thenRemoveDupe() {
		FileInfoDTO officialDocFileInfoDTO = FileInfoDTO.builder()
				.name("offDoc.pdf")
				.uuid(UUID.randomUUID().toString())
				.build();

		Request request = Request.builder()
				.id("1")
				.type(RequestType.builder().code("DUMMY_REQUEST_TYPE").build())
				.build();
		addCaResourceToRequest(CompetentAuthorityEnum.ENGLAND, request);

		UserInfoDTO accountPrimaryContact = UserInfoDTO.builder()
				.firstName("fn").lastName("ln").email("primary@email").userId("primaryUserId")
				.build();

		FileDTO officialDocFileDTO = FileDTO.builder().fileContent("content".getBytes()).build();

		List<String> ccRecipientsEmails = List.of("primary@email", "cc@email");
		List<String> bccRecipientsEmails = List.of("bcc@email");

		CompetentAuthorityDTO competentAuthority = CompetentAuthorityDTO.builder()
				.id(CompetentAuthorityEnum.ENGLAND)
				.name("competentAuthority")
				.email("competent@authority.com")
				.build();

		when(requestAccountContactQueryService.getRequestAccountPrimaryContact(request))
				.thenReturn(Optional.of(accountPrimaryContact));
		when(requestAccountContactQueryService.getRequestAccountServiceContact(request))
				.thenReturn(Optional.of(accountPrimaryContact));
		when(fileDocumentService.getFileDTO(officialDocFileInfoDTO.getUuid()))
				.thenReturn(officialDocFileDTO);
		when(competentAuthorityService.getCompetentAuthorityDTO(CompetentAuthorityEnum.ENGLAND))
				.thenReturn(competentAuthority);

		service.sendOfficialNotice(List.of(officialDocFileInfoDTO), request, ccRecipientsEmails, bccRecipientsEmails);

		verify(requestAccountContactQueryService, times(2)).getRequestAccountPrimaryContact(request);
		verify(requestAccountContactQueryService, times(2)).getRequestAccountServiceContact(request);
		verify(fileDocumentService, times(1)).getFileDTO(officialDocFileInfoDTO.getUuid());
		verify(competentAuthorityService, times(1))
				.getCompetentAuthorityDTO(CompetentAuthorityEnum.ENGLAND);

		ArgumentCaptor<EmailData> emailDataCaptor = ArgumentCaptor.forClass(EmailData.class);
		verify(notificationEmailService, times(1)).notifyRecipients(emailDataCaptor.capture(),
				Mockito.eq(List.of("primary@email")), Mockito.eq(List.of("cc@email")), Mockito.eq(bccRecipientsEmails));
		EmailData emailDataCaptured = emailDataCaptor.getValue();
		assertThat(emailDataCaptured).isEqualTo(EmailData.builder()
				.notificationTemplateData(EmailNotificationTemplateData.builder()
						.templateName(NotificationTemplateName.GENERIC_EMAIL)
						.competentAuthority(CompetentAuthorityEnum.ENGLAND)
						.templateParams(Map.of(
								NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT, accountPrimaryContact.getFullName(),
								NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_FIRST_NAME, accountPrimaryContact.getFirstName(),
								NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_LAST_NAME, accountPrimaryContact.getLastName(),
								NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT, accountPrimaryContact.getFullName(),
								NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_FIRST_NAME, accountPrimaryContact.getFirstName(),
								NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_LAST_NAME, accountPrimaryContact.getLastName(),
								NotificationTemplateConstants.COMPETENT_AUTHORITY_EMAIL, competentAuthority.getEmail(),
								NotificationTemplateConstants.COMPETENT_AUTHORITY_NAME, competentAuthority.getName()
						))
						.build())
				.attachments(Map.of(officialDocFileInfoDTO.getName(), officialDocFileDTO.getFileContent())).build());
	}

	@Test
	void getOfficialNoticeToRecipients() {
		Request request = Request.builder()
				.id("1")
				.build();

		UserInfoDTO accountPrimaryContact = UserInfoDTO.builder()
				.firstName("fn").lastName("ln").email("primary@email").userId("primaryUserId")
				.build();

		UserInfoDTO accountServiceContact = UserInfoDTO.builder()
				.firstName("ab").lastName("cd").email("service@email").userId("serviceUserId")
				.build();

		when(requestAccountContactQueryService.getRequestAccountPrimaryContact(request))
				.thenReturn(Optional.of(accountPrimaryContact));
		when(requestAccountContactQueryService.getRequestAccountServiceContact(request))
				.thenReturn(Optional.of(accountServiceContact));

		Set<UserInfoDTO> defaultOfficialNoticeRecipients = service.getOfficialNoticeToRecipients(request);

		assertEquals(Set.of(accountPrimaryContact, accountServiceContact), defaultOfficialNoticeRecipients);
	}
	
	private void addCaResourceToRequest(CompetentAuthorityEnum competentAuthority, Request request) {
		RequestResource caResource = RequestResource.builder()
				.resourceType(ResourceType.CA)
				.resourceId(competentAuthority.name())
				.request(request)
				.build();

        request.getRequestResources().add(caResource);
	}
}
