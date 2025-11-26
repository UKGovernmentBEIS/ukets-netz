package uk.gov.netz.api.authorization.core.service;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.dto.RoleCode;

import java.util.Set;

/**
 * The role code validator.
 */
@RequiredArgsConstructor
public class RoleCodeValidator implements ConstraintValidator<RoleCode, String> {

    private final RoleService roleService;

    private String roleType;

    @Override
    public void initialize(RoleCode constraintAnnotation) {
        this.roleType = constraintAnnotation.roleType();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        Set<String> codesByType = roleService.getCodesByType(roleType);
        return codesByType.contains(value);
    }
}
