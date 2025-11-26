package uk.gov.netz.api.user.operator.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.operator.service.OperatorAuthorityService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.user.core.service.auth.UserAuthService;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

@Service
@RequiredArgsConstructor
public class OperatorUserRegisteredAcceptInvitationService {

	private final OperatorAuthorityService operatorAuthorityService;
	private final UserRoleTypeService userRoleTypeService;
	private final UserAuthService userAuthService;
	private final OperatorUserNotificationGateway operatorUserNotificationGateway;
	
	public void acceptAuthorityAndNotify(Long authorityId) {
		// accept authority
		final Authority authority = operatorAuthorityService.acceptAuthority(authorityId);
		
		//create user role type if not exist
        userRoleTypeService.createUserRoleTypeIfNotExist(authority.getUserId(), RoleTypeConstants.OPERATOR);
		
		final UserInfoDTO inviteeUser = userAuthService.getUserByUserId(authority.getUserId());
        final UserInfoDTO inviterUser = userAuthService.getUserByUserId(authority.getCreatedBy());

        // Notify invitee and inviter
        operatorUserNotificationGateway.notifyInviteeAcceptedInvitation(inviteeUser);
        operatorUserNotificationGateway.notifyInviterAcceptedInvitation(inviteeUser, inviterUser);
	}
	
}
