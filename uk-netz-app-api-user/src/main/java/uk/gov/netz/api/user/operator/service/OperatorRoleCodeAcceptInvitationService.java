package uk.gov.netz.api.user.operator.service;

import java.util.Set;
import uk.gov.netz.api.user.operator.domain.OperatorUserWithAuthorityDTO;
import uk.gov.netz.api.user.core.domain.enumeration.UserInvitationStatus;

public interface OperatorRoleCodeAcceptInvitationService {

    UserInvitationStatus acceptInvitation(OperatorUserWithAuthorityDTO operatorUserWithAuthorityDTO);

    Set<String> getRoleCodes();
}
