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
public class SicCode {

    public SicCode(String sicCode) {
        this.code = sicCode;
        this.description = CompanyInformationConstantsParser.getSicDescriptions().get(this.code);
    }

    private String code;

    private String description;

}
