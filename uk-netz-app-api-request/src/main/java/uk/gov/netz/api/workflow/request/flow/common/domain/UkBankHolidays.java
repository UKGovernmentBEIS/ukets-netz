package uk.gov.netz.api.workflow.request.flow.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UkBankHolidays {

	/** Holiday events for England and Wales */
    @JsonProperty("england-and-wales")
    private BankHolidaysForCA englandAndWales;

    /** Holiday events for Scotland */
    @JsonProperty("scotland")
    private BankHolidaysForCA scotland;
    
    /** Holiday events for Northern Ireland */
    @JsonProperty("northern-ireland")
    private BankHolidaysForCA northernIreland;
}
