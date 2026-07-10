package uk.gov.netz.api.companieshouse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyProfileDTO {

    private String name;

    private String registrationNumber;

    private CountyAddressDTO address;

    private List<SicCode> sicCodes;

}
