package uk.gov.netz.api.workflow.request.flow.common.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.documenttemplate.domain.templateparams.TemplateParams;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentTemplateOfficialNoticeParamsProvider {
    
    private final DocumentTemplateCommonParamsProvider documentTemplateCommonParamsProvider;
    private final List<DocumentTemplateWorkflowParamsProvider> workflowParamsProviders;
    
    public TemplateParams constructTemplateParams(DocumentTemplateParamsSourceData templateSourceParams) {
        final Request request = templateSourceParams.getRequest();
        final String signatory = templateSourceParams.getSignatory();
        
        final TemplateParams templateParams = documentTemplateCommonParamsProvider.constructCommonTemplateParams(request, signatory);
        
        // Email params
        List<String> ccRecipientsEmailsFinal = new ArrayList<>(templateSourceParams.getCcRecipientsEmails());

        UserInfoDTO accountPrimaryContact = templateSourceParams.getAccountPrimaryContact();

        // PMRV-7236: Case needed for AVIATION DRE UKETS, official notice document needs to be created even if we do not have users assigned to the account
        if (accountPrimaryContact != null) {
            ccRecipientsEmailsFinal.removeIf(email -> email.equals(accountPrimaryContact.getEmail()));
        }

        templateParams.getParams().put("toRecipient", templateSourceParams.getToRecipientEmail());
        templateParams.getParams().put("ccRecipients", ccRecipientsEmailsFinal);
        
        // Workflow params
        workflowParamsProviders.stream()
            .filter(provider -> provider.getContextActionType() == templateSourceParams.getContextActionType())
            .findFirst()
            .ifPresent(workflowParamsProvider -> templateParams.getParams()
                .putAll(workflowParamsProvider.constructParams(templateSourceParams.getRequest().getPayload())));
        
        return templateParams;
    }
}
