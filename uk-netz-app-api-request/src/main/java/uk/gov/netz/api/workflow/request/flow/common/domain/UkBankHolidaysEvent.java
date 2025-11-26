package uk.gov.netz.api.workflow.request.flow.common.domain;

import java.time.LocalDate;

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
public class UkBankHolidaysEvent {

	/** The title for this holiday event. */
    @JsonProperty("title")
    private String title;
    
    /** The date for this holiday event. */
    @JsonProperty("date")
    private LocalDate date;
}
