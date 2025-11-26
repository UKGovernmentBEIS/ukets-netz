package uk.gov.netz.api.authorization.operator.service.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.operator.event.OperatorAuthorityDeletionEvent;

@ExtendWith(MockitoExtension.class)
class OperatorAuthorityDeletionEventListenerTest {

	@InjectMocks
    private OperatorAuthorityDeletionEventListener cut;

    @Mock
    private UserRoleTypeService userRoleTypeService;

    @Test
    void onAuthorityDeletedEvent_exists_other() {
    	OperatorAuthorityDeletionEvent event = OperatorAuthorityDeletionEvent.builder()
    			.userId("userId")
    			.accountId(1L)
    			.existAuthoritiesOnOtherAccounts(true)
    			.build();
    	
        cut.onAuthorityDeletedEvent(event);

        verifyNoInteractions(userRoleTypeService);
    }
    
    @Test
    void onAuthorityDeletedEvent_not_exists_other() {
    	OperatorAuthorityDeletionEvent event = OperatorAuthorityDeletionEvent.builder()
    			.userId("userId")
    			.accountId(1L)
    			.existAuthoritiesOnOtherAccounts(false)
    			.build();
    	
        cut.onAuthorityDeletedEvent(event);

        verify(userRoleTypeService, times(1)).deleteUserRoleType(event.getUserId());
    }
    
}
