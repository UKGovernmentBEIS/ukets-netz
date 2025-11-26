package uk.gov.netz.api.account.service.event;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.account.service.AccountVerificationBodyUnappointService;
import uk.gov.netz.api.verificationbody.event.AccreditationEmissionTradingSchemeNotAvailableEvent;

@RequiredArgsConstructor
@Component
public class AccreditationEmissionTradingSchemeNotAvailableEventListener {
    
    private final AccountVerificationBodyUnappointService accountVerificationBodyUnappointService;

    @EventListener(AccreditationEmissionTradingSchemeNotAvailableEvent.class)
    public void onAccreditationEmissionTradingSchemeNotAvailableEvent(AccreditationEmissionTradingSchemeNotAvailableEvent event) {
		accountVerificationBodyUnappointService.unappointAccountsAppointedToVerificationBodyForEmissionTradingSchemes(
				event.getVerificationBodyId(), event.getNotAvailableAccreditationEmissionTradingSchemes());
    }
}
