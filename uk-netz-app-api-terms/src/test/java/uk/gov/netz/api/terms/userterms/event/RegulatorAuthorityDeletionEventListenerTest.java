package uk.gov.netz.api.terms.userterms.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.regulator.event.RegulatorAuthorityDeletionEvent;
import uk.gov.netz.api.terms.userterms.UserTermsService;

@ExtendWith(MockitoExtension.class)
class RegulatorAuthorityDeletionEventListenerTest {

	@InjectMocks
    private RegulatorAuthorityDeletionEventListener cut;

    @Mock
    private UserTermsService userTermsService;

    @Test
    void onAuthorityDeletedEvent() {
    	RegulatorAuthorityDeletionEvent event = RegulatorAuthorityDeletionEvent.builder()
    			.userId("userId")
    			.build();
    	
        cut.onAuthorityDeletedEvent(event);

        verify(userTermsService, times(1)).deleteUserTerms("userId");
    }
    
}
