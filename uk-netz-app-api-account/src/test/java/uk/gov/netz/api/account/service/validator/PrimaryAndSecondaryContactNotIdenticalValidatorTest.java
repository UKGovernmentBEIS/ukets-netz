package uk.gov.netz.api.account.service.validator;

import org.junit.jupiter.api.Test;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrimaryAndSecondaryContactNotIdenticalValidatorTest {

    private final PrimaryAndSecondaryContactNotIdenticalValidator validator = new PrimaryAndSecondaryContactNotIdenticalValidator();
    
    @Test
    void validateUpdate_no_exception() {
        Long accountId = 1L;
        Map<String, String> contactTypes = Map.of(
                AccountContactType.PRIMARY, "user1",
                AccountContactType.SECONDARY, "user2"
                );
        
        validator.validateUpdate(contactTypes, accountId);
    }
    
    @Test
    void validateUpdate_not_provided() {
        Long accountId = 1L;
        Map<String, String> contactTypes = Map.of(
                AccountContactType.FINANCIAL, "user"
                );
        
        validator.validateUpdate(contactTypes, accountId);
    }
    
    @Test
    void validateUpdate_not_both_provided() {
        Long accountId = 1L;
        Map<String, String> contactTypes = Map.of(
                AccountContactType.PRIMARY, "user"
                );
        
        validator.validateUpdate(contactTypes, accountId);
    }
    
    @Test
    void validateUpdate_identical() {
        Long accountId = 1L;
        Map<String, String> contactTypes = Map.of(
                AccountContactType.PRIMARY, "user",
                AccountContactType.SECONDARY, "user"
                );
        
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            validator.validateUpdate(contactTypes, accountId);
        });
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_CONTACT_TYPE_PRIMARY_AND_SECONDARY_CONTACT_ARE_IDENTICAL);
    }
}
