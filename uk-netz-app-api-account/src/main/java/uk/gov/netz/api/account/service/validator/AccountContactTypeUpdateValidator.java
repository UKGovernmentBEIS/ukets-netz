package uk.gov.netz.api.account.service.validator;

import java.util.Map;

public interface AccountContactTypeUpdateValidator {
    
    void validateUpdate(Map<String, String> contactTypes, Long accountId);
}
