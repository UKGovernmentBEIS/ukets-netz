package uk.gov.netz.api.companieshouse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyType {

    public CompanyType(String companyType) {
        this.code = companyType;
        this.description = CompanyInformationConstantsParser.getCompanyTypes().get(this.code);
    }

    private String code;

    private String description;

}
