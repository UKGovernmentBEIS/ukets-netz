package uk.gov.netz.api.workflow.request.flow.rfi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.documenttemplate.domain.DocumentTemplateType;
import uk.gov.netz.api.documenttemplate.domain.templateparams.TemplateParams;
import uk.gov.netz.api.documenttemplate.service.FileDocumentGenerateServiceDelegator;
import uk.gov.netz.api.files.common.domain.dto.FileInfoDTO;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.DocumentTemplateGenerationContextActionType;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.DocumentTemplateOfficialNoticeParamsProvider;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.DocumentTemplateParamsSourceData;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.OfficialNoticeSendService;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RfiSubmitOfficialNoticeServiceTest {

    @InjectMocks
    private RfiSubmitOfficialNoticeService service;

    @Mock
    private DocumentTemplateOfficialNoticeParamsProvider documentTemplateOfficialNoticeParamsProvider;
    
    @Mock
    private FileDocumentGenerateServiceDelegator fileDocumentGenerateServiceDelegator;
    
    @Mock
    private OfficialNoticeSendService officialNoticeSendService;
    
    @Test
    void generateOfficialNotice() {
        Request request = Request.builder().build();
        addResourcesToRequest(1L, CompetentAuthorityEnum.ENGLAND, request);
        String signatory = "Signatory";
        
        UserInfoDTO accountPrimaryContact = UserInfoDTO.builder()
                .firstName("fn").lastName("ln").email("email@email")
                .build();
        
        List<String> ccRecipientsEmails = List.of("cc1@email", "cc2@email");
        
        DocumentTemplateParamsSourceData templateSourceParams = DocumentTemplateParamsSourceData.builder()
                .request(request)
                .contextActionType(DocumentTemplateGenerationContextActionType.RFI_SUBMIT)
                .accountPrimaryContact(accountPrimaryContact)
                .signatory(signatory)
                .toRecipientEmail(accountPrimaryContact.getEmail())
                .ccRecipientsEmails(ccRecipientsEmails)
                .build();
        TemplateParams templateParams = TemplateParams.builder().build();
        
        
        when(documentTemplateOfficialNoticeParamsProvider.constructTemplateParams(templateSourceParams))
            .thenReturn(templateParams);
        
        //invoke
        service.generateOfficialNotice(request, signatory, accountPrimaryContact, ccRecipientsEmails);
        
        verify(documentTemplateOfficialNoticeParamsProvider, times(1)).constructTemplateParams(templateSourceParams);
        verify(fileDocumentGenerateServiceDelegator, times(1)).generateAndSaveFileDocument(DocumentTemplateType.IN_RFI, templateParams, "Request for Further Information.pdf");
    }
    
    @Test
    void sendOfficialNotice() {
        FileInfoDTO officialNotice = FileInfoDTO.builder()
                .name("official_doc.pdf")
                .uuid(UUID.randomUUID().toString())
                .build();
        
        Request request = Request.builder().build();
        addResourcesToRequest(1L, CompetentAuthorityEnum.ENGLAND, request);
        
        List<String> ccRecipientsEmails = List.of("cc1@email", "cc2@email", "email@email");
        
        // invoke
        service.sendOfficialNotice(officialNotice, request, ccRecipientsEmails);
        
        verify(officialNoticeSendService, times(1)).sendOfficialNotice(List.of(officialNotice), request, ccRecipientsEmails);
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
