package uk.gov.netz.api.workflow.request.flow.common.service.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTemplateParamsSourceData {
    
    private String contextActionType;
    private Request request;
    private String signatory;
    private UserInfoDTO accountPrimaryContact;
    
    private String toRecipientEmail;
    @Builder.Default
    private List<String> ccRecipientsEmails = new ArrayList<>();
}
