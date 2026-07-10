package uk.gov.netz.api.terms.userterms.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.verifier.event.VerifierAuthorityDeletionEvent;
import uk.gov.netz.api.terms.userterms.UserTermsService;

@ExtendWith(MockitoExtension.class)
class VerifierAuthorityDeletionEventListenerTest {

	@InjectMocks
    private VerifierAuthorityDeletionEventListener cut;

    @Mock
    private UserTermsService userTermsService;

    @Test
    void onAuthorityDeletedEvent() {
    	VerifierAuthorityDeletionEvent event = VerifierAuthorityDeletionEvent.builder()
    			.userId("userId")
    			.build();
    	
        cut.onAuthorityDeletedEvent(event);

        verify(userTermsService, times(1)).deleteUserTerms("userId");
    }
    
}
