package uk.gov.netz.api.companieshouse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SicCodeDTO {

    public SicCodeDTO(String sicCode) {
        this.code = sicCode;
    }

    @JsonProperty("code")
    private String code;

    @JsonProperty("description")
    private String description;

}
