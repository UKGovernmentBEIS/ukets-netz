package uk.gov.netz.api.authorization.rules.services.authorization;

import org.apache.commons.lang3.ObjectUtils;

public abstract class AccountAuthorizationService {
    public boolean isApplicable(AuthorizationCriteria criteria) {
        return ObjectUtils.isNotEmpty(criteria.getAccountId());
    }
}
