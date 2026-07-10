package uk.gov.netz.api.terms.userterms.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.operator.event.OperatorAuthorityDeletionEvent;
import uk.gov.netz.api.terms.userterms.UserTermsService;

@ExtendWith(MockitoExtension.class)
class OperatorAuthorityDeletionEventListenerTest {

	@InjectMocks
    private OperatorAuthorityDeletionEventListener cut;

    @Mock
    private UserTermsService userTermsService;

    @Test
    void onAuthorityDeletedEvent_exists_other() {
    	OperatorAuthorityDeletionEvent event = OperatorAuthorityDeletionEvent.builder()
    			.userId("userId")
    			.accountId(1L)
    			.existAuthoritiesOnOtherAccounts(true)
    			.build();
    	
        cut.onAuthorityDeletedEvent(event);

        verifyNoInteractions(userTermsService);
    }
    
    @Test
    void onAuthorityDeletedEvent_not_exists_other() {
    	OperatorAuthorityDeletionEvent event = OperatorAuthorityDeletionEvent.builder()
    			.userId("userId")
    			.accountId(1L)
    			.existAuthoritiesOnOtherAccounts(false)
    			.build();
    	
        cut.onAuthorityDeletedEvent(event);

        verify(userTermsService, times(1)).deleteUserTerms(event.getUserId());
    }
    
}
