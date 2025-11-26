package uk.gov.netz.api.authorization.verifier.service;

import uk.gov.netz.api.authorization.verifier.domain.VerifierAuthorityUpdateDTO;

import java.util.List;

public interface VerifierAuthorityUpdateValidator {

    void validateUpdate(List<VerifierAuthorityUpdateDTO> verifiersUpdate, Long verificationBodyId);
}
