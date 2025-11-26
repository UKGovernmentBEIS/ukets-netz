package uk.gov.netz.api.authorization.operator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.AuthorityConstants;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.Role;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.core.repository.RoleRepository;
import uk.gov.netz.api.authorization.core.service.AuthorityAssignmentService;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.common.utils.UuidGenerator;

import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class OperatorAuthorityService {

    private final AuthorityAssignmentService authorityAssignmentService;
    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;
    private final UserRoleTypeService userRoleTypeService;

    /**
     * Create operator-admin role permission for the provided user and account.
     * @param accountId the account of the operator user
     * @param user the user
     */
    @Transactional
    public void createOperatorAdminAuthority(Long accountId, String user) {
        createOperatorUserAuthorityForRole(accountId, AuthorityConstants.OPERATOR_ADMIN_ROLE_CODE, user,
                AuthorityStatus.ACTIVE, user);
    }

    public Authority acceptAuthority(Long authorityId) {
        return authorityAssignmentService
                .updateAuthorityStatus(authorityId, AuthorityStatus.ACCEPTED);
    }

    /**
     * Creates an authority entry with status {@link AuthorityStatus#PENDING} using the provided input.
     * @param accountId the if of the account related to the authority that will be created
     * @param roleCode the authority role code
     * @param userId the user id to whom the authority will be assigned
     * @param authModificationUser the current logged-in {@link AppUser}
     * @return the {@link Authority} uuid
     */
    @Transactional
    public String createPendingAuthorityForOperator(Long accountId, String roleCode, String userId,
                                                    AppUser authModificationUser) {
        Optional<Authority> userAuthorityForAccountOptional =
                authorityRepository.findByUserIdAndAccountId(userId, accountId);

        Authority userAuthorityForAccount;

        if (userAuthorityForAccountOptional.isPresent()) {
            userAuthorityForAccount = userAuthorityForAccountOptional.get();

            if (AuthorityStatus.PENDING.equals(userAuthorityForAccount.getStatus())) {
                userAuthorityForAccount =
                        authorityAssignmentService.updatePendingAuthority(userAuthorityForAccount, roleCode, authModificationUser.getUserId());
            } else {
                log.warn("Authority for user '{}' in account '{}' exists with code '{}' and status'{}'",
                        userId, accountId, userAuthorityForAccount.getCode(), userAuthorityForAccount.getStatus());
                throw new BusinessException(ErrorCode.AUTHORITY_USER_UPDATE_NON_PENDING_AUTHORITY_NOT_ALLOWED);
            }

        } else {
            userAuthorityForAccount =  createOperatorUserAuthorityForRole(accountId, roleCode, userId, AuthorityStatus.PENDING,
                    authModificationUser.getUserId());
        }

        return userAuthorityForAccount.getUuid();
    }

    @Transactional
    public void createUserRoleType(String userId) {
        userRoleTypeService.createUserRoleTypeOrThrowExceptionIfExists(userId, RoleTypeConstants.OPERATOR);
    }

    private Authority createOperatorUserAuthorityForRole(Long accountId, String roleCode, String userId,
                                                         AuthorityStatus authorityStatus, String authModificationUser) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        Authority authority = Authority.builder()
                .userId(userId)
                .code(role.getCode())
                .accountId(accountId)
                .status(authorityStatus)
                .createdBy(authModificationUser)
                .uuid(authorityStatus.equals(AuthorityStatus.PENDING) ? UuidGenerator.generate() : null)
                .build();

        return authorityAssignmentService.createAuthorityPermissionsForRole(authority, role);
    }

}
