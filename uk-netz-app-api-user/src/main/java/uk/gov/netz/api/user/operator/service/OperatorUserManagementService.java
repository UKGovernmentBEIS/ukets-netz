package uk.gov.netz.api.user.operator.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityDTO;
import uk.gov.netz.api.authorization.operator.service.OperatorAuthorityQueryService;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.user.core.service.UserSecuritySetupService;
import uk.gov.netz.api.user.operator.domain.OperatorUserDTO;
import uk.gov.netz.api.user.operator.domain.OperatorUserStatusDTO;
import uk.gov.netz.api.user.operator.transform.OperatorUserMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OperatorUserManagementService {

    private final OperatorUserAuthService operatorUserAuthService;
    private final OperatorAuthorityQueryService operatorAuthorityQueryService;
    private final UserSecuritySetupService userSecuritySetupService;
    private static final OperatorUserMapper operatorUserMapper = Mappers.getMapper(OperatorUserMapper.class);

    /**
     * Returns the Operator User.
     * @param accountId Account id
     * @param userId Keycloak user id
     * @return {@link OperatorUserDTO}
     */
    public OperatorUserStatusDTO getOperatorUserByAccountAndId(Long accountId, String userId) {
        Optional<AuthorityDTO> authorityOptional = operatorAuthorityQueryService.findAuthorityByUserIdAndAccountId(userId,accountId);
        // Validate user
        final AuthorityStatus authorityStatus =
                authorityOptional.map(AuthorityDTO::getStatus).orElseThrow(() -> new BusinessException(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_ACCOUNT));
        OperatorUserDTO operatorUserDTO = operatorUserAuthService.getUserById(userId);
        return operatorUserMapper.toOperatorUserStatusDTO(operatorUserDTO, authorityStatus);
    }


    public void updateOperatorUser(AppUser appUser, OperatorUserDTO operatorUserDTO) {
        if (appUser.getEmail().equals(operatorUserDTO.getEmail())) {
            operatorUserAuthService.updateUser(operatorUserDTO);
        } else throw new BusinessException(ErrorCode.USER_NOT_LOGGED_IN_USER);
    }

    /**
     * Updates the Operator User.
     * @param accountId Account id
     * @param userId Keycloak user id
     * @param operatorUserDTO {@link OperatorUserDTO}
     */
    public void updateOperatorUserByAccountAndId(Long accountId, String userId, OperatorUserDTO operatorUserDTO) {
        Optional<AuthorityDTO> authorityOptional = operatorAuthorityQueryService.findAuthorityByUserIdAndAccountId(userId,accountId);
        // Validate editing user
        final AuthorityStatus authorityStatus =
                authorityOptional.map(AuthorityDTO::getStatus).orElseThrow(() -> new BusinessException(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_ACCOUNT));
        validateUserStatus(authorityStatus);
        // Update user
        operatorUserAuthService.updateUser(operatorUserDTO);
    }
    
	public void resetOperator2Fa(Long accountId, String userId) {
        Optional<AuthorityDTO> authorityOptional = operatorAuthorityQueryService.findAuthorityByUserIdAndAccountId(userId,accountId);
        // Validate editing user
        final AuthorityStatus authorityStatus =
                authorityOptional.map(AuthorityDTO::getStatus).orElseThrow(() -> new BusinessException(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_ACCOUNT));
        validateUserStatus(authorityStatus);
		userSecuritySetupService.resetUser2Fa(userId);
	}

    private void validateUserStatus(AuthorityStatus authorityStatus) {
        if (!authorityStatus.equals(AuthorityStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.USER_INVALID_STATUS);
        }
    }
}
