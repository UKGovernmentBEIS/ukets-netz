package uk.gov.netz.api.user.operator.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.user.application.UserRoleTypeAuthService;
import uk.gov.netz.api.user.core.service.auth.AuthService;
import uk.gov.netz.api.user.core.service.auth.UserAuthService;
import uk.gov.netz.api.user.operator.domain.OperatorUserDTO;
import uk.gov.netz.api.user.operator.domain.OperatorUserInvitationDTO;
import uk.gov.netz.api.user.operator.domain.OperatorUserRegistrationDTO;
import uk.gov.netz.api.user.operator.domain.OperatorUserRegistrationWithCredentialsDTO;
import uk.gov.netz.api.user.operator.transform.OperatorUserMapper;
import uk.gov.netz.api.user.operator.transform.OperatorUserRegistrationMapper;

@Service
@RequiredArgsConstructor
public class OperatorUserAuthService implements UserRoleTypeAuthService<OperatorUserDTO> {

	private final AuthService authService;
	private final UserAuthService userAuthService;
    private final OperatorUserMapper operatorUserMapper;
    private final OperatorUserRegistrationMapper operatorUserRegistrationMapper;
    
    @Override
    public String getRoleType() {
        return RoleTypeConstants.OPERATOR;
    }
    
    @Override
    public OperatorUserDTO getUserById(String userId) {
        return operatorUserMapper.toOperatorUserDTO(authService.getUserRepresentationById(userId));
    }
    
    @Override
	public OperatorUserDTO getCurrentUserDTO(AppUser currentUser) {
		return getUserById(currentUser.getUserId());
	}
    
    public Optional<String> getUserIdByEmail(String email) {
    	return userAuthService.getUserByEmail(email).map(userInfo -> userInfo.getUserId());
    }

	public OperatorUserDTO registerAndEnableOperatorUser(
			OperatorUserRegistrationWithCredentialsDTO operatorUserRegistrationWithCredentialsDTO, String email) {
		UserRepresentation userRepresentation = operatorUserRegistrationMapper
				.toUserRepresentation(operatorUserRegistrationWithCredentialsDTO, email);

		authService.registerAndEnableUserAndSetPassword(userRepresentation, operatorUserRegistrationWithCredentialsDTO.getPassword());
		
    	return operatorUserMapper.toOperatorUserDTO(userRepresentation);
    }

    public String registerOperatorUser(OperatorUserInvitationDTO operatorUserInvitation) {
    	return updateUser(operatorUserInvitation);
    }
    
    public String updateUser(OperatorUserInvitationDTO operatorUserInvitation) {
        UserRepresentation userRepresentation = operatorUserMapper.toUserRepresentation(operatorUserInvitation);
        return authService.saveUser(userRepresentation);
    }

    public void updateUser(OperatorUserDTO updatedOperatorUserDTO) {
        UserRepresentation updatedUser = operatorUserMapper.toUserRepresentation(updatedOperatorUserDTO);
        authService.saveUser(updatedUser);
    }

	public OperatorUserDTO enableAndUpdateUserAndSetPassword(
			OperatorUserRegistrationWithCredentialsDTO operatorUserRegistrationWithCredentialsDTO, String userId) {
        UserRepresentation keycloakUser = authService.getUserRepresentationById(userId);
        
        UserRepresentation userRepresentation = operatorUserRegistrationMapper
				.toUserRepresentation(operatorUserRegistrationWithCredentialsDTO, keycloakUser.getEmail());
        
		authService.enableAndSaveUserAndSetPassword(userRepresentation, operatorUserRegistrationWithCredentialsDTO.getPassword());

        return operatorUserMapper.toOperatorUserDTO(userRepresentation);
    }

	public OperatorUserDTO enableAndUpdateUser(OperatorUserRegistrationDTO operatorUserRegistrationDTO, String userId) {
        UserRepresentation keycloakUser = authService.getUserRepresentationById(userId);

        UserRepresentation userRepresentation = operatorUserRegistrationMapper
				.toUserRepresentation(operatorUserRegistrationDTO, keycloakUser.getEmail());

        authService.enableAndSaveUser(userRepresentation);
        return operatorUserMapper.toOperatorUserDTO(userRepresentation);
    }

    public OperatorUserDTO setUserPassword(String userId, String password) {
        UserRepresentation userRepresentation = authService.setUserPassword(userId, password);
        return operatorUserMapper.toOperatorUserDTO(userRepresentation);
    }

}
