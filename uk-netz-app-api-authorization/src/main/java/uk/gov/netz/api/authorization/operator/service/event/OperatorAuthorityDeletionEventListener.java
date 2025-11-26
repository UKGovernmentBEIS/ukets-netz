package uk.gov.netz.api.authorization.operator.service.event;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.operator.event.OperatorAuthorityDeletionEvent;

@RequiredArgsConstructor
@Component(value = "authorizationOperatorAuthorityDeletionEventListener")
public class OperatorAuthorityDeletionEventListener {

    private final UserRoleTypeService userRoleTypeService;

    @Order(100)
    @EventListener(OperatorAuthorityDeletionEvent.class)
    public void onAuthorityDeletedEvent(OperatorAuthorityDeletionEvent deletionEvent) {
		if (!deletionEvent.isExistAuthoritiesOnOtherAccounts()) {
			userRoleTypeService.deleteUserRoleType(deletionEvent.getUserId());
		}
    }
}
