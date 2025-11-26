package uk.gov.netz.api.user.regulator.service;

import java.util.Optional;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.files.common.domain.dto.FileDTO;
import uk.gov.netz.api.user.application.UserRoleTypeAuthService;
import uk.gov.netz.api.user.core.domain.model.UserDetails;
import uk.gov.netz.api.user.core.service.UserSignatureValidatorService;
import uk.gov.netz.api.user.core.service.auth.AuthService;
import uk.gov.netz.api.user.core.service.auth.UserAuthService;
import uk.gov.netz.api.user.core.service.auth.UserDetailsSaveException;
import uk.gov.netz.api.user.core.transform.UserDetailsMapper;
import uk.gov.netz.api.user.regulator.domain.RegulatorCurrentUserDTO;
import uk.gov.netz.api.user.regulator.domain.RegulatorInvitedUserDetailsDTO;
import uk.gov.netz.api.user.regulator.domain.RegulatorUserDTO;
import uk.gov.netz.api.user.regulator.transform.RegulatorInviteUserMapper;
import uk.gov.netz.api.user.regulator.transform.RegulatorUserMapper;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

@Service
@RequiredArgsConstructor
public class RegulatorUserAuthService implements UserRoleTypeAuthService<RegulatorUserDTO> {

	private final AuthService authService;
	private final UserAuthService userAuthService;
    private final UserSignatureValidatorService userSignatureValidatorService;
    
    private final RegulatorUserMapper regulatorUserMapper;
    private final RegulatorInviteUserMapper regulatorInviteUserMapper;
    private final UserDetailsMapper userDetailsMapper;
    
    @Override
    public String getRoleType() {
        return RoleTypeConstants.REGULATOR;
    }

    @Override
    public RegulatorUserDTO getUserById(String userId) {
        UserRepresentation userRep = authService.getUserRepresentationById(userId);
        return regulatorUserMapper.toRegulatorUserDTO(
                userRep, 
                authService.getUserDetails(userId).map(UserDetails::getSignature).orElse(null)
                );
    }
    
    @Override
	public RegulatorCurrentUserDTO getCurrentUserDTO(AppUser currentUser) {
    	final String userId = currentUser.getUserId();
    	final RegulatorUserDTO regulatorUserDTO = getUserById(userId);
		return regulatorUserMapper.toRegulatorCurrentUserDTO(regulatorUserDTO, currentUser.getCompetentAuthority());
	}
    
    public Optional<UserInfoDTO> getUserByEmail(String email) {
    	return userAuthService.getUserByEmail(email);
    }
    
    public String registerRegulatorInvitedUser(RegulatorInvitedUserDetailsDTO regulatorUserInvitation, FileDTO signature) {
        userSignatureValidatorService.validateSignature(signature);
        
        UserRepresentation newUserRepresentation = regulatorInviteUserMapper.toUserRepresentation(regulatorUserInvitation);
        String userId = authService.saveUser(newUserRepresentation);

        try {
            authService.saveUserDetails(
                    userDetailsMapper.toUserDetails(userId, signature));
        } catch (UserDetailsSaveException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER, e);
        }
        
        return userId;
    }
    
    public void updateRegulatorUser(String userId, RegulatorUserDTO newRegulatorUserDTO, FileDTO signature) {
        userSignatureValidatorService.validateSignature(signature);
        
        UserRepresentation registeredUser = authService.getUserRepresentationById(userId);
        
        UserRepresentation updatedUser = regulatorUserMapper.toUserRepresentation(newRegulatorUserDTO);
        authService.saveUser(updatedUser);
        
        try {
            authService.saveUserDetails(
                    userDetailsMapper.toUserDetails(userId, signature));
        } catch (UserDetailsSaveException e) {
            //rollback update
            authService.saveUser(registeredUser);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER, e);
        }
    }

}
