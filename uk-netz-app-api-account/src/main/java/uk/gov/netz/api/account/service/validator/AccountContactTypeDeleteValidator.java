package uk.gov.netz.api.account.service.validator;

import java.util.Map;

public interface AccountContactTypeDeleteValidator {

    void validateDelete(Map<String, String> contactTypes);
}
