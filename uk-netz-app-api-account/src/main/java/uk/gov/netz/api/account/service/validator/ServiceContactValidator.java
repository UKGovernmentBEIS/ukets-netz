package uk.gov.netz.api.account.service.validator;

import org.springframework.stereotype.Component;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.Map;

@Component
public class ServiceContactValidator implements AccountContactTypeUpdateValidator, AccountContactTypeDeleteValidator {

    @Override
    public void validateUpdate(Map<String, String> contactTypes, Long accountId) {
        validateServiceContactExists(contactTypes);
    }

    @Override
    public void validateDelete(Map<String, String> contactTypes) {
        validateServiceContactExists(contactTypes);
    }

    private void validateServiceContactExists(Map<String, String> contactTypes) {
        String userId = contactTypes.get(AccountContactType.SERVICE);
        if (userId == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_CONTACT_TYPE_SERVICE_CONTACT_IS_REQUIRED);
        }
    }
}
