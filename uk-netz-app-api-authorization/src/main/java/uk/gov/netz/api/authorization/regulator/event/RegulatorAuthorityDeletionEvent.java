package uk.gov.netz.api.authorization.regulator.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.authorization.core.domain.AuthorityDeletionEvent;

@Getter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class RegulatorAuthorityDeletionEvent extends AuthorityDeletionEvent {
	
}
