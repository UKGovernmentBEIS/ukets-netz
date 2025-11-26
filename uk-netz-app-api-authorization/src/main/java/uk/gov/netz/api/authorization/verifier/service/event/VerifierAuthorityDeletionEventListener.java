package uk.gov.netz.api.authorization.verifier.service.event;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.verifier.event.VerifierAuthorityDeletionEvent;

@RequiredArgsConstructor
@Component(value = "authorizationVerifierAuthorityDeletionEventListener")
public class VerifierAuthorityDeletionEventListener {

    private final UserRoleTypeService userRoleTypeService;

    @Order(100)
    @EventListener(VerifierAuthorityDeletionEvent.class)
    public void onAuthorityDeletedEvent(VerifierAuthorityDeletionEvent deletionEvent) {
    	userRoleTypeService.deleteUserRoleType(deletionEvent.getUserId());
    }
}
