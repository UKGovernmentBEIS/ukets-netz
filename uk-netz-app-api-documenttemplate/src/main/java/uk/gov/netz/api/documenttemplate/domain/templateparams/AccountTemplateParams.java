package uk.gov.netz.api.documenttemplate.domain.templateparams;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class AccountTemplateParams {

    private String name;
    private CompetentAuthorityEnum competentAuthority;
    private String location;
    
    private String primaryContact; //full name
    private String primaryContactEmail;
    
    private String serviceContact; //full name
    private String serviceContactEmail;
}
