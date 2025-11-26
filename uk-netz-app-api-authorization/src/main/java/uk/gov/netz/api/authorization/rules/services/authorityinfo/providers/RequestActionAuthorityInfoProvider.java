package uk.gov.netz.api.authorization.rules.services.authorityinfo.providers;

import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.RequestActionAuthorityInfoDTO;

public interface RequestActionAuthorityInfoProvider {
    RequestActionAuthorityInfoDTO getRequestActionAuthorityInfo(Long requestActionId);
}
