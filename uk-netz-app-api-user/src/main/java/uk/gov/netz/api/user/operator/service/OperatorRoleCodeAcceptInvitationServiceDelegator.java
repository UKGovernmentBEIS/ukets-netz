package uk.gov.netz.api.user.operator.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.user.operator.domain.OperatorUserWithAuthorityDTO;
import uk.gov.netz.api.user.core.domain.enumeration.UserInvitationStatus;

@Service
@RequiredArgsConstructor
public class OperatorRoleCodeAcceptInvitationServiceDelegator {

    private final List<OperatorRoleCodeAcceptInvitationService> operatorRoleCodeAcceptInvitationServices;

    @Transactional
	public UserInvitationStatus acceptInvitation(OperatorUserWithAuthorityDTO operatorUserWithAuthorityDTO,
			String roleCode) {
		return operatorRoleCodeAcceptInvitationServices.stream()
				.filter(service -> service.getRoleCodes().contains(roleCode))
				.findAny()
				.map(service -> service.acceptInvitation(operatorUserWithAuthorityDTO))
				.orElseThrow(() -> new BusinessException(ErrorCode.USER_REGISTRATION_FAILED_500));
    }

}
