package uk.gov.netz.api.workflow.request.application.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.rules.services.resource.RegulatorAuthorityResourceService;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegulatorAuthorityResourceAdapter {
    private final RegulatorAuthorityResourceService regulatorAuthorityResourceService;

    public Map<CompetentAuthorityEnum, Set<String>> getUserScopedRequestTaskTypes(String userId) {
        return regulatorAuthorityResourceService.findUserScopedRequestTaskTypes(userId);
    }
}
