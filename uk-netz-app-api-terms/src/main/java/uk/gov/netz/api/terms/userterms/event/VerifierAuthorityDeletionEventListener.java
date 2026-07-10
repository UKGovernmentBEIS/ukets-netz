package uk.gov.netz.api.terms.userterms.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.verifier.event.VerifierAuthorityDeletionEvent;
import uk.gov.netz.api.terms.userterms.UserTermsService;

@RequiredArgsConstructor
@Component(value = "termsVerifierAuthorityDeletionEventListener")
@ConditionalOnProperty(prefix = "ui.features", name = "terms", havingValue = "true")
public class VerifierAuthorityDeletionEventListener {

    private final UserTermsService userTermsService;

    @Order(200)
    @EventListener(VerifierAuthorityDeletionEvent.class)
    public void onAuthorityDeletedEvent(VerifierAuthorityDeletionEvent deletionEvent) {
		userTermsService.deleteUserTerms(deletionEvent.getUserId());
    }
}
