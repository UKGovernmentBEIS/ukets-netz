package uk.gov.netz.api.user.operator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.AuthorityConstants;
import uk.gov.netz.api.user.operator.domain.OperatorUserWithAuthorityDTO;
import uk.gov.netz.api.user.core.domain.enumeration.UserInvitationStatus;

@ExtendWith(MockitoExtension.class)
class EmitterContactAcceptInvitationServiceTest {

    @InjectMocks
    private EmitterContactAcceptInvitationService emitterContactAcceptInvitationService;

    @Mock
    private OperatorUserRegisteredAcceptInvitationService operatorUserRegisteredAcceptInvitationService;

    @Test
    void acceptInvitation_user_authentication_not_Enabled() {
        OperatorUserWithAuthorityDTO operatorUserAcceptInvitation = OperatorUserWithAuthorityDTO.builder()
            .enabled(false)
            .build();

        UserInvitationStatus userInvitationStatus =
            emitterContactAcceptInvitationService.acceptInvitation(operatorUserAcceptInvitation);

        assertEquals(UserInvitationStatus.PENDING_TO_REGISTERED_SET_REGISTER_FORM_NO_PASSWORD, userInvitationStatus);

        verifyNoInteractions(operatorUserRegisteredAcceptInvitationService);
    }

    @Test
    void acceptInvitation_user_enabled() {
        Long authorityId = 1L;
        OperatorUserWithAuthorityDTO operatorUserAcceptInvitation = OperatorUserWithAuthorityDTO.builder()
        		.enabled(true)
            .userAuthorityId(authorityId)
            .build();

        UserInvitationStatus userInvitationStatus = emitterContactAcceptInvitationService
                .acceptInvitation(operatorUserAcceptInvitation);

        assertEquals(UserInvitationStatus.ACCEPTED, userInvitationStatus);

        verify(operatorUserRegisteredAcceptInvitationService, times(1))
                .acceptAuthorityAndNotify(authorityId);
    }

    @Test
    void getRoleCodes() {
        assertThat(emitterContactAcceptInvitationService.getRoleCodes()).containsOnly(AuthorityConstants.EMITTER_CONTACT);
    }
}