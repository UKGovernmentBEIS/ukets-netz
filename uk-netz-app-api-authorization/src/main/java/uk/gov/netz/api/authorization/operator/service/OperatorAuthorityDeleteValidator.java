package uk.gov.netz.api.authorization.operator.service;

import uk.gov.netz.api.authorization.core.domain.Authority;

public interface OperatorAuthorityDeleteValidator {

    void validateDeletion(Authority authority);
}