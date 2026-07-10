package uk.gov.netz.api.mireport.userdefined.custom;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.mireport.userdefined.MiReportUserDefinedGeneratorDelegator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ValidSqlQueryValidatorTest {

    @InjectMocks
    private ValidSqlQueryValidator validator;

    @Mock
    private MiReportUserDefinedGeneratorDelegator delegator;

    @Test
    void isValid_validQuery_returnsTrue() {
        assertThat(validator.isValid("select * from facility_audit", null)).isTrue();
        verify(delegator, times(1)).validateQuery("select * from facility_audit");
    }

    @Test
    void isValid_invalidQuery_returnsFalse() {
        doThrow(new BusinessException(ErrorCode.CUSTOM_REPORT_ERROR))
                .when(delegator).validateQuery("drop table facility_audit");

        assertThat(validator.isValid("drop table facility_audit", null)).isFalse();
    }

    @Test
    void isValid_blank() {
        assertThat(validator.isValid("  ", null)).isTrue();
        assertThat(validator.isValid(null, null)).isTrue();
        verifyNoInteractions(delegator);
    }
}