package uk.gov.netz.api.authorization.thirdpartydataprovider.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.AuthorityConstants;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.Role;
import uk.gov.netz.api.authorization.core.repository.RoleRepository;
import uk.gov.netz.api.authorization.core.service.AuthorityAssignmentService;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.common.utils.UuidGenerator;

@Log4j2
@Service
@RequiredArgsConstructor
public class ThirdPartyDataProviderAuthorityService {

    private final RoleRepository roleRepository;
    private final AuthorityAssignmentService authorityAssignmentService;

    /**
     * Creates an authority entry with status {@link AuthorityStatus#ACTIVE} using the provided input.
     * @param thirdPartyDataProviderId the third party data provider id related to the authority that will be created
     * @param serviceAccountUserId the user id of the default keycloak service account user to whom the authority will be assigned
     * @param authCreationUser the user id of current logged-in user
     * @return the created authority uuid
     */
    public Authority createActiveAuthorityForRole(Long thirdPartyDataProviderId,
                                                  String serviceAccountUserId, String authCreationUser) {
        Role role = roleRepository.findByCode(AuthorityConstants.THIRD_PARTY_DATA_PROVIDER)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        Authority authority = Authority.builder()
            .userId(serviceAccountUserId)
            .code(role.getCode())
            .status(AuthorityStatus.ACTIVE)
            .thirdPartyDataProviderId(thirdPartyDataProviderId)
            .createdBy(authCreationUser)
            .uuid(UuidGenerator.generate())
            .build();

        return authorityAssignmentService.createAuthorityPermissionsForRole(authority, role);
    }

}
