package uk.gov.netz.api.authorization.core.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityDTO;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityInfoDTO;

public interface AuthorityService<T extends AuthorityDTO> {

	/**
     * Returns the list of active assigned authorities for the provided user id. Is only used under Security Context.
     *
     * @param userId Keycloak user id
     * @return List of {@link T}
     */
	List<T> getActiveAuthoritiesWithAssignedPermissions(String userId);
	
	/**
     * Returns the list of Authorities for the provided user id.
     *
     * @param userId Keycloak user id
     * @return List of {@link AuthorityDTO}
     */
	List<AuthorityDTO> getAuthoritiesByUserId(String userId);
	
	/**
     * Find the list of assigned permissions for the given user id.
     *
     * @param userId Keycloak user id
     * @return the permissions of the user with the given id
     */
	List<String> findAssignedPermissionsByUserId(String userId);
	
	boolean existsByUserId(String userId);
	
	boolean existsByUserIdAndAccountId(String userId, Long accountId);
	
	Optional<AuthorityInfoDTO> findAuthorityByUuidAndStatusPending(String uuid);
	
	Map<String, AuthorityStatus> findStatusByUsersAndAccountId(List<String> userIds, Long accountId);
	
	Map<String, AuthorityStatus> findStatusByUsers(List<String> userIds);
	
	Optional<AuthorityInfoDTO> findAuthorityByUserIdAndAccountId(String userId, Long accountId);
	
}
