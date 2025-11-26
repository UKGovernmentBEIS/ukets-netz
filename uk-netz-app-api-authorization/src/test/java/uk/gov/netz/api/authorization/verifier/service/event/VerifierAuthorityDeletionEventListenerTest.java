package uk.gov.netz.api.authorization.verifier.service.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.verifier.event.VerifierAuthorityDeletionEvent;

@ExtendWith(MockitoExtension.class)
class VerifierAuthorityDeletionEventListenerTest {

	@InjectMocks
    private VerifierAuthorityDeletionEventListener cut;

    @Mock
    private UserRoleTypeService userRoleTypeService;

    @Test
    void onAuthorityDeletedEvent() {
    	VerifierAuthorityDeletionEvent event = VerifierAuthorityDeletionEvent.builder()
    			.userId("userId")
    			.build();
    	
        cut.onAuthorityDeletedEvent(event);

        verify(userRoleTypeService, times(1)).deleteUserRoleType("userId");
    }
    
}
