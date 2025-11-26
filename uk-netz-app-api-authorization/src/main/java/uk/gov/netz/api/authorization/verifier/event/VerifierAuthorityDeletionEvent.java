package uk.gov.netz.api.authorization.verifier.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.authorization.core.domain.AuthorityDeletionEvent;

@SuperBuilder
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
public class VerifierAuthorityDeletionEvent extends AuthorityDeletionEvent {

}
