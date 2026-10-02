package uk.gov.netz.api.workflow.request.flow.common.service.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityDTO;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.competentauthority.CompetentAuthorityService;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.files.documents.service.storage.FileDocumentStorageService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private FileDocumentStorageService fileDocumentStorageService;

    @Mock
    private CompetentAuthorityService competentAuthorityService;

    @Test
    void sendOfficialNotice_sameServiceContact() {
        DeliveryTestData testData = prepareDeliveryTestData(true);

        service.sendOfficialNotice(List.of(testData.fileInfo()), testData.request());

        EmailData<EmailNotificationTemplateData> emailData = verifyNotification(
                testData, List.of(testData.primaryContact().getEmail()), List.of(), List.of());
        assertAttachmentDelivery(emailData, testData);
    }

    @Test
    void sendOfficialNotice_withCcAndSameServiceContact() {
        DeliveryTestData testData = prepareDeliveryTestData(true);
        List<String> ccRecipientsEmails = List.of("cc1@email", "cc2@email");

        service.sendOfficialNotice(List.of(testData.fileInfo()), testData.request(), ccRecipientsEmails);

        EmailData<EmailNotificationTemplateData> emailData = verifyNotification(
                testData, List.of(testData.primaryContact().getEmail()), ccRecipientsEmails, List.of());
        assertAttachmentDelivery(emailData, testData);
    }

    @Test
    void sendOfficialNotice_withCcAndDifferentServiceContact() {
        DeliveryTestData testData = prepareDeliveryTestData(false);
        List<String> ccRecipientsEmails = List.of("cc1@email", "cc2@email");

        service.sendOfficialNotice(List.of(testData.fileInfo()), testData.request(), ccRecipientsEmails);

        ArgumentCaptor<EmailData<EmailNotificationTemplateData>> emailDataCaptor = emailDataCaptor();
        ArgumentCaptor<List<String>> toRecipientsEmailsCaptor = listCaptor();
        verify(notificationEmailService).notifyRecipients(
                emailDataCaptor.capture(),
                toRecipientsEmailsCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(ccRecipientsEmails),
                org.mockito.ArgumentMatchers.eq(Collections.emptyList()));
        assertThat(toRecipientsEmailsCaptor.getValue()).containsExactlyInAnyOrder("primary@email", "service@email");
        verifySharedDependencies(testData);
        assertTemplateData(emailDataCaptor.getValue(), testData);
        assertAttachmentDelivery(emailDataCaptor.getValue(), testData);
    }

    @Test
    void sendOfficialNotice_withCcAndBccWhenDuplicatesInCc_thenRemoveDupe() {
        DeliveryTestData testData = prepareDeliveryTestData(true);
        List<String> ccRecipientsEmails = List.of(testData.primaryContact().getEmail(), "cc@email");
        List<String> bccRecipientsEmails = List.of("bcc@email");

        service.sendOfficialNotice(
                List.of(testData.fileInfo()), testData.request(), ccRecipientsEmails, bccRecipientsEmails);

        EmailData<EmailNotificationTemplateData> emailData = verifyNotification(
                testData,
                List.of(testData.primaryContact().getEmail()),
                List.of("cc@email"),
                bccRecipientsEmails);
        assertAttachmentDelivery(emailData, testData);
    }

    @Test
    void sendOfficialNoticeAsLinks_withoutCcOrBcc() {
        DeliveryTestData testData = prepareDeliveryTestData(true);

        service.sendOfficialNoticeAsLinks(List.of(testData.fileInfo()), testData.request());

        EmailData<EmailNotificationTemplateData> emailData = verifyNotification(
                testData, List.of(testData.primaryContact().getEmail()), List.of(), List.of());
        assertLinkedFileDelivery(emailData, testData);
    }

    @Test
    void sendOfficialNoticeAsLinks_withCc() {
        DeliveryTestData testData = prepareDeliveryTestData(true);
        List<String> ccRecipientsEmails = List.of("cc1@email", "cc2@email");

        service.sendOfficialNoticeAsLinks(
                List.of(testData.fileInfo()), testData.request(), ccRecipientsEmails);

        EmailData<EmailNotificationTemplateData> emailData = verifyNotification(
                testData, List.of(testData.primaryContact().getEmail()), ccRecipientsEmails, List.of());
        assertLinkedFileDelivery(emailData, testData);
    }

    @Test
    void sendOfficialNoticeAsLinks_withCcAndBccWhenDuplicatesInCc_thenRemoveDupe() {
        DeliveryTestData testData = prepareDeliveryTestData(true);
        List<String> ccRecipientsEmails = List.of(testData.primaryContact().getEmail(), "cc@email");
        List<String> bccRecipientsEmails = List.of("bcc@email");

        service.sendOfficialNoticeAsLinks(
                List.of(testData.fileInfo()), testData.request(), ccRecipientsEmails, bccRecipientsEmails);

        EmailData<EmailNotificationTemplateData> emailData = verifyNotification(
                testData,
                List.of(testData.primaryContact().getEmail()),
                List.of("cc@email"),
                bccRecipientsEmails);
        assertLinkedFileDelivery(emailData, testData);
    }

    @Test
    void getOfficialNoticeToRecipients() {
        Request request = Request.builder()
                .id("1")
                .build();
        UserInfoDTO accountPrimaryContact = buildUser("fn", "ln", "primary@email", "primaryUserId");
        UserInfoDTO accountServiceContact = buildUser("ab", "cd", "service@email", "serviceUserId");

        when(requestAccountContactQueryService.getRequestAccountPrimaryContact(request))
                .thenReturn(Optional.of(accountPrimaryContact));
        when(requestAccountContactQueryService.getRequestAccountServiceContact(request))
                .thenReturn(Optional.of(accountServiceContact));

        Set<UserInfoDTO> defaultOfficialNoticeRecipients = service.getOfficialNoticeToRecipients(request);

        assertEquals(Set.of(accountPrimaryContact, accountServiceContact), defaultOfficialNoticeRecipients);
    }

    @ParameterizedTest
    @CsvSource({"0,true,true", "1,true,true", "2,true,true", "0,true,false", "1,true,false", "2,true,false",
        "0,false,true", "1,false,true", "2,false,true", "0,false,false", "1,false,false", "2,false,false"})
    void mixedOverloadsSupportEveryFileCombination(int overload, boolean attachments, boolean links) {
        DeliveryTestData testData = prepareDeliveryTestData(true, attachments || links);
        List<FileInfoDTO> attachedFiles = attachments ? List.of(testData.fileInfo()) : List.of();
        List<FileInfoDTO> linkedFiles = links ? List.of(testData.fileInfo()) : List.of();
        List<String> cc = List.of(testData.primaryContact().getEmail(), "cc@email");
        List<String> bcc = List.of("bcc@email");

        switch (overload) {
            case 0 -> service.sendOfficialNotice(attachedFiles, linkedFiles, testData.request());
            case 1 -> service.sendOfficialNotice(attachedFiles, linkedFiles, testData.request(), cc);
            default -> service.sendOfficialNotice(attachedFiles, linkedFiles, testData.request(), cc, bcc);
        }

        ArgumentCaptor<EmailData<EmailNotificationTemplateData>> captured = emailDataCaptor();
        verify(notificationEmailService).notifyRecipients(captured.capture(), eq(List.of(testData.primaryContact().getEmail())),
                eq(overload == 0 ? List.of() : List.of("cc@email")), eq(overload == 2 ? bcc : List.of()));
        EmailData<EmailNotificationTemplateData> email = captured.getValue();
        Map<String, byte[]> expectedFiles = Map.of(testData.fileInfo().getName(), testData.file().getFileContent());
        assertThat(email.getAttachments()).containsExactlyInAnyOrderEntriesOf(attachments ? expectedFiles : Map.of());
        assertThat(email.getLinkedFiles()).containsExactlyInAnyOrderEntriesOf(links ? expectedFiles : Map.of());
        assertTemplateData(email, testData);
        if (attachments || links) {
            verifySharedDependencies(testData);
        } else {
            verifyNoInteractions(fileDocumentStorageService);
        }
    }

    @Test
    void differentFilesWithTheSameNameStayIndependentAcrossCollections() {
        DeliveryTestData testData = prepareDeliveryTestData(true);
        FileInfoDTO linked = FileInfoDTO.builder().name(testData.fileInfo().getName()).uuid(UUID.randomUUID().toString()).build();
        byte[] linkedContent = new byte[]{4, 5};
        when(fileDocumentStorageService.getFileDTO(linked.getUuid())).thenReturn(FileDTO.builder().fileContent(linkedContent).build());

        service.sendOfficialNotice(List.of(testData.fileInfo()), List.of(linked), testData.request());

        EmailData<EmailNotificationTemplateData> email = verifyNotification(testData,
                List.of(testData.primaryContact().getEmail()), List.of(), List.of());
        assertThat(email.getAttachments()).containsEntry(testData.fileInfo().getName(), testData.file().getFileContent());
        assertThat(email.getLinkedFiles()).containsEntry(linked.getName(), linkedContent);
        verify(fileDocumentStorageService).getFileDTO(linked.getUuid());
    }

    @ParameterizedTest
    @CsvSource({"true,true,true", "true,true,false", "true,false,true", "true,false,false",
        "false,true,true", "false,true,false", "false,false,true", "false,false,false"})
    void duplicateNamesFailBeforeLoadingEitherCollection(boolean attachments, boolean sameUuid, boolean mixed) {
        DeliveryTestData testData = prepareDeliveryTestData(true, false);
        FileInfoDTO second = FileInfoDTO.builder().name(testData.fileInfo().getName())
                .uuid(sameUuid ? testData.fileInfo().getUuid() : UUID.randomUUID().toString()).build();
        List<FileInfoDTO> duplicate = List.of(testData.fileInfo(), second);
        List<FileInfoDTO> otherFiles = mixed
                ? List.of(FileInfoDTO.builder().name("other.pdf").uuid(UUID.randomUUID().toString()).build())
                : List.of();

        assertThatThrownBy(() -> service.sendOfficialNotice(attachments ? duplicate : otherFiles,
                attachments ? otherFiles : duplicate, testData.request())).isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(fileDocumentStorageService, notificationEmailService);
    }

    @Test
    void caseSensitiveNamesForTheSameDocumentShareOneContentRead() {
        DeliveryTestData testData = prepareDeliveryTestData(true);
        FileInfoDTO lowerCase = FileInfoDTO.builder().name("notice.pdf").uuid(testData.fileInfo().getUuid()).build();
        FileInfoDTO upperCase = FileInfoDTO.builder().name("Notice.pdf").uuid(testData.fileInfo().getUuid()).build();

        service.sendOfficialNotice(List.of(lowerCase, upperCase), List.of(lowerCase), testData.request());

        EmailData<EmailNotificationTemplateData> email = verifyNotification(testData,
                List.of(testData.primaryContact().getEmail()), List.of(), List.of());
        assertThat(email.getAttachments()).containsExactlyInAnyOrderEntriesOf(Map.of(
                lowerCase.getName(), testData.file().getFileContent(), upperCase.getName(), testData.file().getFileContent()));
        assertThat(email.getLinkedFiles()).containsExactlyInAnyOrderEntriesOf(Map.of(lowerCase.getName(), testData.file().getFileContent()));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void legacyOverloadsInvokeTheExistingDeliveryOverride(int overload) {
        OfficialNoticeSendService customizedDelivery = mock(OfficialNoticeSendService.class);
        OfficialNoticeSendService subclass = new OfficialNoticeSendService(requestAccountContactQueryService,
                notificationEmailService, fileDocumentStorageService, competentAuthorityService) {
            @Override
            public void sendOfficialNotice(List<FileInfoDTO> attachments, Request request, List<String> cc, List<String> bcc) {
                customizedDelivery.sendOfficialNotice(attachments, request, cc, bcc);
            }
        };
        Request request = Request.builder().id("legacy").build();
        List<FileInfoDTO> attachments = List.of(FileInfoDTO.builder().name("notice.pdf").uuid("document").build());
        List<String> cc = List.of("cc@email");

        if (overload == 0) {
            subclass.sendOfficialNotice(attachments, request);
        } else {
            subclass.sendOfficialNotice(attachments, request, cc);
        }

        verify(customizedDelivery).sendOfficialNotice(attachments, request, overload == 0 ? List.of() : cc, List.of());
        verifyNoInteractions(requestAccountContactQueryService, notificationEmailService, fileDocumentStorageService, competentAuthorityService);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void everyDeliveryModeHonorsThePublicRecipientOverride(int mode) {
        DeliveryTestData testData = prepareDeliveryTestData(false);
        UserInfoDTO customRecipient = buildUser("Custom", "Recipient", "custom@email", "customUser");
        OfficialNoticeSendService subclass = new OfficialNoticeSendService(requestAccountContactQueryService,
                notificationEmailService, fileDocumentStorageService, competentAuthorityService) {
            @Override
            public Set<UserInfoDTO> getOfficialNoticeToRecipients(Request request) {
                assertThat(request).isSameAs(testData.request());
                return Set.of(customRecipient);
            }
        };
        List<FileInfoDTO> files = List.of(testData.fileInfo());
        List<String> cc = List.of("custom@email", "remaining@email");
        List<String> bcc = List.of("bcc@email");

        switch (mode) {
            case 0 -> subclass.sendOfficialNotice(files, testData.request(), cc, bcc);
            case 1 -> subclass.sendOfficialNoticeAsLinks(files, testData.request(), cc, bcc);
            default -> subclass.sendOfficialNotice(files, files, testData.request(), cc, bcc);
        }

        ArgumentCaptor<EmailData<EmailNotificationTemplateData>> captured = emailDataCaptor();
        verify(notificationEmailService).notifyRecipients(captured.capture(), eq(List.of("custom@email")), eq(List.of("remaining@email")), eq(bcc));
        assertTemplateData(captured.getValue(), testData);
        Map<String, byte[]> content = Map.of(testData.fileInfo().getName(), testData.file().getFileContent());
        assertThat(captured.getValue().getAttachments()).containsExactlyInAnyOrderEntriesOf(mode == 1 ? Map.of() : content);
        assertThat(captured.getValue().getLinkedFiles()).containsExactlyInAnyOrderEntriesOf(mode == 0 ? Map.of() : content);
        verify(requestAccountContactQueryService).getRequestAccountPrimaryContact(testData.request());
        verify(requestAccountContactQueryService).getRequestAccountServiceContact(testData.request());
        verify(fileDocumentStorageService).getFileDTO(testData.fileInfo().getUuid());
    }

    private DeliveryTestData prepareDeliveryTestData(boolean sameServiceContact) {
        return prepareDeliveryTestData(sameServiceContact, true);
    }

    private DeliveryTestData prepareDeliveryTestData(boolean sameServiceContact, boolean loadFile) {
        FileInfoDTO fileInfo = FileInfoDTO.builder()
                .name("offDoc.pdf")
                .uuid(UUID.randomUUID().toString())
                .build();
        FileDTO file = FileDTO.builder().fileContent("content".getBytes()).build();
        Request request = Request.builder()
                .id("1")
                .type(RequestType.builder().code("DUMMY_REQUEST_TYPE").build())
                .build();
        addCaResourceToRequest(CompetentAuthorityEnum.ENGLAND, request);
        UserInfoDTO primaryContact = buildUser("fn", "ln", "primary@email", "primaryUserId");
        UserInfoDTO serviceContact = sameServiceContact
                ? primaryContact
                : buildUser("ab", "cd", "service@email", "serviceUserId");
        CompetentAuthorityDTO competentAuthority = CompetentAuthorityDTO.builder()
                .id(CompetentAuthorityEnum.ENGLAND)
                .name("competentAuthority")
                .email("competent@authority.com")
                .build();

        when(requestAccountContactQueryService.getRequestAccountPrimaryContact(request))
                .thenReturn(Optional.of(primaryContact));
        when(requestAccountContactQueryService.getRequestAccountServiceContact(request))
                .thenReturn(Optional.of(serviceContact));
        if (loadFile) {
            when(fileDocumentStorageService.getFileDTO(fileInfo.getUuid())).thenReturn(file);
        }
        when(competentAuthorityService.getCompetentAuthorityDTO(CompetentAuthorityEnum.ENGLAND))
                .thenReturn(competentAuthority);

        return new DeliveryTestData(
                request, fileInfo, file, primaryContact, serviceContact, competentAuthority);
    }

    private EmailData<EmailNotificationTemplateData> verifyNotification(
            DeliveryTestData testData,
            List<String> toRecipientsEmails,
            List<String> ccRecipientsEmails,
            List<String> bccRecipientsEmails) {
        ArgumentCaptor<EmailData<EmailNotificationTemplateData>> emailDataCaptor = emailDataCaptor();
        verify(notificationEmailService).notifyRecipients(
                emailDataCaptor.capture(),
                eq(toRecipientsEmails),
                eq(ccRecipientsEmails),
                eq(bccRecipientsEmails));
        verifySharedDependencies(testData);
        assertTemplateData(emailDataCaptor.getValue(), testData);
        return emailDataCaptor.getValue();
    }

    private void verifySharedDependencies(DeliveryTestData testData) {
        verify(requestAccountContactQueryService, times(2)).getRequestAccountPrimaryContact(testData.request());
        verify(requestAccountContactQueryService, times(2)).getRequestAccountServiceContact(testData.request());
        verify(fileDocumentStorageService, times(1)).getFileDTO(testData.fileInfo().getUuid());
        verify(competentAuthorityService, times(1))
                .getCompetentAuthorityDTO(CompetentAuthorityEnum.ENGLAND);
    }

    private void assertTemplateData(EmailData<EmailNotificationTemplateData> emailData,
                                    DeliveryTestData testData) {
        assertThat(emailData.getNotificationTemplateData()).isEqualTo(
                EmailNotificationTemplateData.builder()
                        .templateName(NotificationTemplateName.GENERIC_EMAIL)
                        .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                        .templateParams(Map.of(
                                NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT,
                                testData.primaryContact().getFullName(),
                                NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_FIRST_NAME,
                                testData.primaryContact().getFirstName(),
                                NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_LAST_NAME,
                                testData.primaryContact().getLastName(),
                                NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT,
                                testData.serviceContact().getFullName(),
                                NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_FIRST_NAME,
                                testData.serviceContact().getFirstName(),
                                NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_LAST_NAME,
                                testData.serviceContact().getLastName(),
                                NotificationTemplateConstants.COMPETENT_AUTHORITY_EMAIL,
                                testData.competentAuthority().getEmail(),
                                NotificationTemplateConstants.COMPETENT_AUTHORITY_NAME,
                                testData.competentAuthority().getName()))
                        .build());
    }

    private void assertAttachmentDelivery(EmailData<EmailNotificationTemplateData> emailData,
                                          DeliveryTestData testData) {
        assertThat(emailData.getAttachments()).containsOnlyKeys(testData.fileInfo().getName());
        assertThat(emailData.getAttachments().get(testData.fileInfo().getName()))
                .isEqualTo(testData.file().getFileContent());
        assertThat(emailData.getLinkedFiles()).isEmpty();
    }

    private void assertLinkedFileDelivery(EmailData<EmailNotificationTemplateData> emailData,
                                          DeliveryTestData testData) {
        assertThat(emailData.getAttachments()).isEmpty();
        assertThat(emailData.getLinkedFiles()).containsOnlyKeys(testData.fileInfo().getName());
        assertThat(emailData.getLinkedFiles().get(testData.fileInfo().getName()))
                .isEqualTo(testData.file().getFileContent());
    }

    private UserInfoDTO buildUser(String firstName, String lastName, String email, String userId) {
        return UserInfoDTO.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .userId(userId)
                .build();
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<EmailData<EmailNotificationTemplateData>> emailDataCaptor() {
        return ArgumentCaptor.forClass(EmailData.class);
    }

    @SuppressWarnings("unchecked")
    private ArgumentCaptor<List<String>> listCaptor() {
        return ArgumentCaptor.forClass(List.class);
    }

    private void addCaResourceToRequest(CompetentAuthorityEnum competentAuthority, Request request) {
        RequestResource caResource = RequestResource.builder()
                .resourceType(ResourceType.CA)
                .resourceId(competentAuthority.name())
                .request(request)
                .build();
        request.getRequestResources().add(caResource);
    }

    private record DeliveryTestData(
            Request request,
            FileInfoDTO fileInfo,
            FileDTO file,
            UserInfoDTO primaryContact,
            UserInfoDTO serviceContact,
            CompetentAuthorityDTO competentAuthority) {
    }
}
