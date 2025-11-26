package uk.gov.netz.api.workflow.request.flow.common.domain;

import java.util.List;

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
public class BankHolidaysForCA {

	/** The CA division. */
    @JsonProperty("division")
    private String division;
    
    /** The holiday events for this CA division. */
    @JsonProperty("events")
    private List<UkBankHolidaysEvent> events;
    
}
