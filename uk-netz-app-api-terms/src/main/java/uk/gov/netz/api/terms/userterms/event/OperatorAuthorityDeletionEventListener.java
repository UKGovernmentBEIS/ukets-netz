package uk.gov.netz.api.terms.userterms.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.operator.event.OperatorAuthorityDeletionEvent;
import uk.gov.netz.api.terms.userterms.UserTermsService;

@RequiredArgsConstructor
@Component(value = "termsOperatorAuthorityDeletionEventListener")
@ConditionalOnProperty(prefix = "ui.features", name = "terms", havingValue = "true")
public class OperatorAuthorityDeletionEventListener {

    private final UserTermsService userTermsService;

    @Order(200)
    @EventListener(OperatorAuthorityDeletionEvent.class)
    public void onAuthorityDeletedEvent(OperatorAuthorityDeletionEvent deletionEvent) {
		if (!deletionEvent.isExistAuthoritiesOnOtherAccounts()) {
			userTermsService.deleteUserTerms(deletionEvent.getUserId());
		}
    }
}
