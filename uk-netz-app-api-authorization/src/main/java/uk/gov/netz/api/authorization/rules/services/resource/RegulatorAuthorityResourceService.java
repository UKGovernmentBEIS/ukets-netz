package uk.gov.netz.api.authorization.rules.services.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegulatorAuthorityResourceService {

    private final AuthorityRepository authorityRepository;

    public Map<CompetentAuthorityEnum, Set<String>> findUserScopedRequestTaskTypes(String userId) {
        return authorityRepository
            .findResourceSubTypesRegulatorUserHasScope(userId, ResourceType.REQUEST_TASK, Scope.REQUEST_TASK_VIEW);
    }
    
    public List<String> findUsersWithScopeOnResourceTypeAndSubTypeAndCA(
            String resourceType, String resourceSubType, String scope, CompetentAuthorityEnum competentAuthority) {
        return authorityRepository.findRegulatorUsersWithScopeOnResourceTypeAndResourceSubTypeAndCA(
                resourceType, resourceSubType, scope, competentAuthority);
    }
    
    public List<String> findUsersByCompetentAuthority(CompetentAuthorityEnum competentAuthority) {
        return authorityRepository.findRegulatorUsersByCompetentAuthority(competentAuthority);
    }
    
}
