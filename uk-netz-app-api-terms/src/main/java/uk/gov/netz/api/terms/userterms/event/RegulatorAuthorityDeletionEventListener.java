package uk.gov.netz.api.terms.userterms.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.regulator.event.RegulatorAuthorityDeletionEvent;
import uk.gov.netz.api.terms.userterms.UserTermsService;

@RequiredArgsConstructor
@Component(value = "termsRegulatorAuthorityDeletionEventListener")
@ConditionalOnProperty(prefix = "ui.features", name = "terms", havingValue = "true")
public class RegulatorAuthorityDeletionEventListener {

    private final UserTermsService userTermsService;

    @Order(200)
    @EventListener(RegulatorAuthorityDeletionEvent.class)
    public void onAuthorityDeletedEvent(RegulatorAuthorityDeletionEvent deletionEvent) {
		userTermsService.deleteUserTerms(deletionEvent.getUserId());
    }
}
