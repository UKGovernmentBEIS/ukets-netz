package uk.gov.netz.api.authorization.thirdpartydataprovider.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.AuthorityConstants;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.Role;
import uk.gov.netz.api.authorization.core.repository.RoleRepository;
import uk.gov.netz.api.authorization.core.service.AuthorityAssignmentService;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.common.utils.UuidGenerator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class ThirdPartyDataProviderAuthorityServiceTest {

    @InjectMocks
    private ThirdPartyDataProviderAuthorityService service;

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AuthorityAssignmentService authorityAssignmentService;

    @Test
    void createActiveAuthorityForRole() {
        Long thirdPartyDataProviderId = 1L;
        String serviceAccountUserId = "serviceAccountUserId";
        String authCreationUser = "authCreationUser";
        String thirdPartyDataProviderCode = "THIRD_PARTY_DATA_PROVIDER";
        Role role = Role.builder().code(thirdPartyDataProviderCode).build();
        Authority authority = Authority.builder()
            .userId(serviceAccountUserId)
            .code(role.getCode())
            .status(AuthorityStatus.ACTIVE)
            .thirdPartyDataProviderId(thirdPartyDataProviderId)
            .createdBy(authCreationUser)
            .uuid(UuidGenerator.generate())
            .build();

        when(roleRepository.findByCode(AuthorityConstants.THIRD_PARTY_DATA_PROVIDER))
            .thenReturn(Optional.of(role));

        service.createActiveAuthorityForRole(thirdPartyDataProviderId, serviceAccountUserId, authCreationUser);

        verify(roleRepository).findByCode(AuthorityConstants.THIRD_PARTY_DATA_PROVIDER);
        verify(authorityAssignmentService).createAuthorityPermissionsForRole(authority, role);
        verifyNoMoreInteractions(roleRepository, authorityAssignmentService);
    }

    @Test
    void createActiveAuthorityForRole_throws_exception() {
        Long thirdPartyDataProviderId = 1L;
        String serviceAccountUserId = "serviceAccountUserId";
        String authCreationUser = "authCreationUser";

        when(roleRepository.findByCode(AuthorityConstants.THIRD_PARTY_DATA_PROVIDER)).thenReturn(Optional.empty());

        BusinessException businessException = assertThrows(BusinessException.class, () ->
            service.createActiveAuthorityForRole(thirdPartyDataProviderId, serviceAccountUserId, authCreationUser));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, businessException.getErrorCode());

        verify(roleRepository).findByCode(AuthorityConstants.THIRD_PARTY_DATA_PROVIDER);
        verifyNoMoreInteractions(roleRepository);
        verifyNoInteractions(authorityAssignmentService);
    }
}