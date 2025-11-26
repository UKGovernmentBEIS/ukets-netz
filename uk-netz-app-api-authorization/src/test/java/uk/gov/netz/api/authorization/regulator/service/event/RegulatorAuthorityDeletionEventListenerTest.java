package uk.gov.netz.api.authorization.regulator.service.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.regulator.event.RegulatorAuthorityDeletionEvent;

@ExtendWith(MockitoExtension.class)
class RegulatorAuthorityDeletionEventListenerTest {

	@InjectMocks
    private RegulatorAuthorityDeletionEventListener cut;

    @Mock
    private UserRoleTypeService userRoleTypeService;

    @Test
    void onAuthorityDeletedEvent() {
    	RegulatorAuthorityDeletionEvent event = RegulatorAuthorityDeletionEvent.builder()
    			.userId("userId")
    			.build();
    	
        cut.onAuthorityDeletedEvent(event);

        verify(userRoleTypeService, times(1)).deleteUserRoleType("userId");
    }
    
}
