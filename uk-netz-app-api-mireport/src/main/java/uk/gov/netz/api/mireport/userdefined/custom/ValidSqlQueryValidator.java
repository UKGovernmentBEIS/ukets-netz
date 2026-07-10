package uk.gov.netz.api.mireport.userdefined.custom;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.mireport.userdefined.MiReportUserDefinedGeneratorDelegator;

@Component
@RequiredArgsConstructor
public class ValidSqlQueryValidator implements ConstraintValidator<ValidSqlQuery, String> {

    private final MiReportUserDefinedGeneratorDelegator miReportUserDefinedGeneratorDelegator;

    @Override
    public boolean isValid(String sqlQuery, ConstraintValidatorContext context) {
        if (sqlQuery == null || sqlQuery.isBlank()) {
            return true;
        }
        try {
            miReportUserDefinedGeneratorDelegator.validateQuery(sqlQuery);
            return true;
        } catch (RuntimeException ex) {
            if (ex instanceof BusinessException) {
                return false;
            }
            throw ex;
        }
    }
}