package uk.gov.netz.integration.model.regulatornotice;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class ReturnOfAllowancesRegulatorNoticeEvent extends RegulatorNoticeEvent {

    private LocalDate returnDate;

}
