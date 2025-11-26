package uk.gov.netz.api.account.service.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.account.service.AccountVerificationBodyUnappointService;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.verificationbody.event.AccreditationEmissionTradingSchemeNotAvailableEvent;

import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccreditationEmissionTradingSchemeNotAvailableEventListenerTest {
    
    @InjectMocks
    private AccreditationEmissionTradingSchemeNotAvailableEventListener listener;

    @Mock
    private AccountVerificationBodyUnappointService accountVerificationBodyUnappointService;

    @Test
    void onAccreditationEmissionTradingSchemeNotAvailableEvent() {
        Long verificationBodyId = 1L;
        Set<String> notAvailableAccreditationEmissionTradingSchemes = Set.of(TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME.getName());
		AccreditationEmissionTradingSchemeNotAvailableEvent event = new AccreditationEmissionTradingSchemeNotAvailableEvent(
				verificationBodyId, notAvailableAccreditationEmissionTradingSchemes);

        listener.onAccreditationEmissionTradingSchemeNotAvailableEvent(event);

        verify(accountVerificationBodyUnappointService,times(1))
            .unappointAccountsAppointedToVerificationBodyForEmissionTradingSchemes(verificationBodyId, notAvailableAccreditationEmissionTradingSchemes);
    }

}
