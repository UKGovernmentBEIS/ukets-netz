package uk.gov.netz.api.authorization.operator.event;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.authorization.core.domain.AuthorityDeletionEvent;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OperatorAuthorityDeletionEvent extends AuthorityDeletionEvent {

    private Long accountId;
    private boolean existAuthoritiesOnOtherAccounts;
}
