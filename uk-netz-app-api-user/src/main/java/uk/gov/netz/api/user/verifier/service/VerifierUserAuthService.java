package uk.gov.netz.api.user.verifier.service;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.user.application.UserRoleTypeAuthService;
import uk.gov.netz.api.user.core.service.auth.AuthService;
import uk.gov.netz.api.user.core.service.auth.UserAuthService;
import uk.gov.netz.api.user.verifier.domain.VerifierUserDTO;
import uk.gov.netz.api.user.verifier.domain.VerifierUserInvitationDTO;
import uk.gov.netz.api.user.verifier.transform.VerifierUserMapper;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

@Service
@RequiredArgsConstructor
public class VerifierUserAuthService implements UserRoleTypeAuthService<VerifierUserDTO> {

    private final AuthService authService;
    private final UserAuthService userAuthService;
    private final VerifierUserMapper verifierUserMapper;
    
    @Override
    public String getRoleType() {
        return RoleTypeConstants.VERIFIER;
    }

    @Override
    public VerifierUserDTO getUserById(String userId) {
        return verifierUserMapper.toVerifierUserDTO(authService.getUserRepresentationById(userId));
    }
    
    @Override
	public VerifierUserDTO getCurrentUserDTO(AppUser currentUser) {
		return getUserById(currentUser.getUserId());
	}

    public Optional<UserInfoDTO> getUserByEmail(String email) {
    	return userAuthService.getUserByEmail(email);
    }

    public void updateVerifierUser(VerifierUserDTO verifierUserDTO) {
        UserRepresentation updatedUser = verifierUserMapper.toUserRepresentation(verifierUserDTO);
        authService.saveUser(updatedUser);
    }

    /**
     * @param verifierUserInvitation the {@link VerifierUserInvitationDTO} containing invited user's info.
     * @return the unique id for the user
     */
    @Transactional
    public String registerInvitedVerifierUser(VerifierUserInvitationDTO verifierUserInvitation) {
        UserRepresentation newUserRepresentation = verifierUserMapper.toUserRepresentation(verifierUserInvitation);
        return authService.saveUser(newUserRepresentation);
    }

}
