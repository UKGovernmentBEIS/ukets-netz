package uk.gov.netz.api.authorization.regulator.service.event;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.regulator.event.RegulatorAuthorityDeletionEvent;

@RequiredArgsConstructor
@Component(value = "authorizationRegulatorAuthorityDeletionEventListener")
public class RegulatorAuthorityDeletionEventListener {

    private final UserRoleTypeService userRoleTypeService;

    @Order(100)
    @EventListener(RegulatorAuthorityDeletionEvent.class)
    public void onAuthorityDeletedEvent(RegulatorAuthorityDeletionEvent deletionEvent) {
    	userRoleTypeService.deleteUserRoleType(deletionEvent.getUserId());
    }
}
